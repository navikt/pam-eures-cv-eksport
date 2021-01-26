package no.nav.cv.eures.konverterer.esco

import com.fasterxml.jackson.module.kotlin.*
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.cv.eures.konverterer.esco.dto.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


@Service
class JanzzService(
        private val client: JanzzClient,
        private val janzzCacheRepository: JanzzCacheRepository,

        @Value("\${janzz.authorization.token}") private val token: String,
        @Value("\${janzz.labels.resultLimit}") private val resultLimit: String
) : InitializingBean {

    companion object {
        private lateinit var instance: JanzzService
        fun instance() = instance
    }

    override fun afterPropertiesSet() {
        instance = this
    }

    enum class EscoLookup {
        LOOKUP_CONCEPT,
        LOOKUP_TERM
    }

    private val log: Logger = LoggerFactory.getLogger(JanzzService::class.java)
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    fun getEscoForConceptId(conceptId: String): List<CachedEscoMapping> = getEsco(conceptId, EscoLookup.LOOKUP_CONCEPT)

    fun getEscoForCompetence(term: String): List<CachedEscoMapping> = getEsco(term, EscoLookup.LOOKUP_TERM)

    private fun getEsco(searchFor: String, lookup: EscoLookup): List<CachedEscoMapping> {
        val cachedEsco = when (lookup) {
            EscoLookup.LOOKUP_CONCEPT -> janzzCacheRepository.fetchFromCacheConceptId(searchFor)
            EscoLookup.LOOKUP_TERM -> janzzCacheRepository.fetchFromCacheTerm(searchFor)
        }

        log.info("Cache for $lookup $searchFor contains ${cachedEsco.size} hits")

        // TODO As soon as Janzz finishes updating all their ESCO codes to the new format,
        // remove this check to be more future proof wrt url changes
        // 69
        // 74 http://data.europa.eu/esco/occupation/303a1e34-cb16-4054-b323-81e5eec17397
        //
        return when {
            cachedEsco.isNotEmpty() -> cachedEsco
            else -> fetchAndSaveToCache(searchFor, lookup)
        }.filter { it.esco.length == 69 || it.esco.length == 74 }

    }


    private fun fetchAndSaveToCache(searchFor: String, lookup: EscoLookup): List<CachedEscoMapping> {
        val queryResult = when (lookup) {
            EscoLookup.LOOKUP_CONCEPT -> queryJanzzConceptId(searchFor)
            EscoLookup.LOOKUP_TERM -> queryJanzzTerm(searchFor)
        }

        janzzCacheRepository.saveToCache(queryResult)

        val exactHits = when (lookup) {
            EscoLookup.LOOKUP_CONCEPT -> queryResult.filter { it.conceptId == searchFor }
            EscoLookup.LOOKUP_TERM -> queryResult.filter { it.term == searchFor }
        }

        log.info("Saved ${queryResult.size} results to cache and returning ${exactHits.size} hit(s) from service")

        return exactHits
    }

    private fun queryJanzzConceptId(conceptId: String): List<CachedEscoMapping> {
        val authorization = "token $token"

        val startMillis = System.currentTimeMillis()

        val json = client.lookupConceptId(
                authorization = authorization,
                conceptId = conceptId)

        if (json == null) {
            log.error("Janzz query for concept $conceptId returned null")
            return listOf()
        }

        val spentMillis = System.currentTimeMillis() - startMillis

        val concept = objectMapper.readValue<JanzzEscoConceptMapping>(json)

        log.info("Query for concept '$conceptId' yielded result $concept in $spentMillis ms")

        return concept
                .classificationSet
                .filter { it.classification == "ESCO" }
                .map {CachedEscoMapping(
                        concept.preferredLabel,
                        concept.id.toString(),
                        it.value, ZonedDateTime.now()
                )}
    }

    private fun queryJanzzTerm(term: String): List<CachedEscoMapping> {
        val authorization = "token $token"

        val startMillis = System.currentTimeMillis()

        val json = client.search(
                authorization = authorization,
                query = term,
                limit = resultLimit)

        if (json == null) {
            log.error("Janzz query for term $term returned null")
            return listOf()
        }

        val spentMillis = System.currentTimeMillis() - startMillis

        val res = objectMapper.readValue<List<JanzzEscoLabelMapping>>(json)

        log.info("Query for term '$term' yielded ${res.size} result(s) (limit $resultLimit) in $spentMillis ms")

        return res.flatMap { outer ->
            outer.classifications.ESCO.map { esco ->
                CachedEscoMapping(outer.label,
                        outer.conceptId.toString(), esco, ZonedDateTime.now())
            }
        }
    }
}

