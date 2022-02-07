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
        OCCUPATION,
        COMPETENCE
    }

    private val log: Logger = LoggerFactory.getLogger(JanzzService::class.java)
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    fun getEscoForOccupation(conceptTitle: String): List<CachedEscoMapping> = getEsco(conceptTitle, EscoLookup.OCCUPATION)

    fun getEscoForCompetence(term: String): List<CachedEscoMapping> = getEsco(term, EscoLookup.COMPETENCE)

    fun getTermForEsco(escoCode: String): String? = janzzCacheRepository.getCacheForEsco(escoCode)

    fun getEscoForTerm(term: String, escoLookup: EscoLookup) {
        val cachedEsco = janzzCacheRepository.fetchFromCacheTerm(term)

        log.info("Cache for $escoLookup $term contains ${cachedEsco.size} hits")

        // TODO As soon as Janzz finishes updating all their ESCO codes to the new format,
        // remove this check to be more future proof wrt url changes

        // USING IT TO FILTER NEGATIVE CACHE HITS TOO

        // 69 http://data.europa.eu/esco/skill/148fc290-6363-4b0a-90a6-fe2f998f2037
        // 74 http://data.europa.eu/esco/occupation/303a1e34-cb16-4054-b323-81e5eec17397
        //
        return when {
            cachedEsco.isNotEmpty() -> cachedEsco
            else -> fetchAndSaveToCache(term, escoLookup)
        }
            .filter { it.esco.length == 69 || it.esco.length == 74 }
            .also {log.info("Esco of type $lookup for $searchFor returned ${it.size} filtered hits")}

    }

    private fun getEsco(searchFor: String, lookup: EscoLookup): List<CachedEscoMapping> {
        val cachedEsco = janzzCacheRepository.fetchFromCacheTerm(searchFor)

        log.info("Cache for $lookup $searchFor contains ${cachedEsco.size} hits")

        // TODO As soon as Janzz finishes updating all their ESCO codes to the new format,
        // remove this check to be more future proof wrt url changes

        // USING IT TO FILTER NEGATIVE CACHE HITS TOO

        // 69 http://data.europa.eu/esco/skill/148fc290-6363-4b0a-90a6-fe2f998f2037
        // 74 http://data.europa.eu/esco/occupation/303a1e34-cb16-4054-b323-81e5eec17397
        //
        return when {
            cachedEsco.isNotEmpty() -> cachedEsco
            else -> fetchAndSaveToCache(searchFor, lookup)
        }
                .filter { it.esco.length == 69 || it.esco.length == 74 }
                .also {log.info("Esco of type $lookup for $searchFor returned ${it.size} filtered hits")}

    }


    private fun fetchAndSaveToCache(term: String, escoLookup: EscoLookup): List<CachedEscoMapping> {
        val queryResult = when (lookup) {
            EscoLookup.LOOKUP_CONCEPT -> queryJanzzOccupation(searchFor)
            EscoLookup.LOOKUP_TERM -> queryJanzzTerm(searchFor)
        }

        janzzCacheRepository.saveToCache(queryResult)

        val exactHits = when (lookup) {
            //BURDE VÆRE TERM ELLER SEARCHFOR BURDE VÆRE KONSEPTID
            //EscoLookup.LOOKUP_CONCEPT -> queryResult.filter { it.conceptId == searchFor }
            EscoLookup.LOOKUP_CONCEPT -> queryResult.filter { it.term == searchFor }
            EscoLookup.LOOKUP_TERM -> queryResult.filter { it.term == searchFor }
        }

        log.info("Saved ${queryResult.size} results to cache and returning ${exactHits.size} hit(s) from service")

        return exactHits
    }

    private fun queryJanzzOccupation(term: String): List<CachedEscoMapping> {
        val authorization = "token $token"

        val startMillis = System.currentTimeMillis()

        val json = client.lookUpOccupation(
                authorization = authorization,
                conceptTitle = term)

        if (json == null || json.length < 3) {
            log.error("Janzz query for concept $term returned null or empty list")
            return listOf(CachedEscoMapping(
                    term = term,
                    conceptId = "",
                    esco = "NO HIT",
                    updated = ZonedDateTime.now()))

        }

        val spentMillis = System.currentTimeMillis() - startMillis

        val concepts = objectMapper.readValue<Array<JanzzEscoConceptMapping>>(json)

        log.info("Query for concept '$term' yielded result $concepts in $spentMillis ms")

        val hits = concepts
            .flatMap { outer ->
                outer.classifications
                .ESCO
                .map { url ->
                    CachedEscoMapping(
                        term = outer.preferredLabel,
                        conceptId = outer.id.toString(),
                        esco = url,
                        updated = ZonedDateTime.now()
                    )
                }
            }

        return hits.ifEmpty {
            listOf(CachedEscoMapping(
                term = term,
                conceptId = "",
                esco = "NO HIT",
                updated = ZonedDateTime.now()))
        }
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
            return listOf(CachedEscoMapping(
                term = term,
                conceptId = "",
                esco = "NO HIT",
                updated = ZonedDateTime.now()))
        }

        val spentMillis = System.currentTimeMillis() - startMillis

        val res = objectMapper.readValue<List<JanzzEscoLabelMapping>>(json)

        log.info("Query for term '$term' yielded ${res.size} result(s) (limit $resultLimit) in $spentMillis ms")

        val hits =  res.flatMap { outer ->
            outer.classifications.ESCO.map { esco ->
                CachedEscoMapping(
                        term = outer.label,
                        conceptId = outer.conceptId.toString(),
                        esco = esco,
                        updated = ZonedDateTime.now())
            }
        }

        return hits.ifEmpty {
            listOf(CachedEscoMapping(
                term = term,
                conceptId = "",
                esco = "NO HIT",
                updated = ZonedDateTime.now()))
        }
    }
}

