package no.nav.cv.eures.konverterer.esco

import com.fasterxml.jackson.module.kotlin.*
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.cv.eures.konverterer.esco.dto.CachedEscoMapping
import no.nav.cv.eures.konverterer.esco.dto.JanzzEscoMapping
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


@Service
class JanzzService (
        private val client: JanzzClient,
        private val janzzCacheRepository: JanzzCacheRepository,

        @Value("\${janzz.authorization.token}") private val token: String,
        @Value("\${janzz.labels.resultLimit}") private val resultLimit: String
) : InitializingBean {

    companion object {
        private lateinit var instance : JanzzService
        fun instance() = instance
    }

    override fun afterPropertiesSet() {
        instance = this
    }

    private val log: Logger = LoggerFactory.getLogger(JanzzService::class.java)
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    fun getEscoForCompetence(term: String) : List<CachedEscoMapping> {
        val cachedEsco = janzzCacheRepository.fetchFromCache(term)

        log.info("Cache for term $term contains ${cachedEsco.size} hits")

        val escoMappings = if(cachedEsco.isNotEmpty()) cachedEsco
        else fetchAndSaveToCache(term)

        // TODO As soon as Janzz finishes updating all their ESCO codes to the new format,
        // remove this check to be more future proof wrt url changes
        return escoMappings.filter { it.esco.length == 69 }
    }

    private fun fetchAndSaveToCache(term: String) : List<CachedEscoMapping> {
        val queryResult = convertJanzzToCache(queryJanzz(term))

        janzzCacheRepository.saveToCache(queryResult)

        val exactHits = queryResult.filter { it.term == term }

        log.info("Saved ${queryResult.size} results to cache and returning ${exactHits.size} hit(s) from service")

        return exactHits
    }

    private fun queryJanzz(term: String) : List<JanzzEscoMapping> {
        val authorization = "token $token"

        val startMillis = System.currentTimeMillis()

        val json = client.search(
                authorization = authorization,
                query = term,
                limit = resultLimit)

        if(json == null) {
            log.error("Janzz query for term $term returned null")
            return listOf()
        }

        val spentMillis = System.currentTimeMillis() - startMillis

        val res = objectMapper.readValue(json, object : TypeReference<List<JanzzEscoMapping>>(){})

        log.info("Query for '$term' yielded ${res.size} result(s) (limit $resultLimit) in $spentMillis ms")

        return res
    }

    private fun convertJanzzToCache(janzzMappings: List<JanzzEscoMapping>) : List<CachedEscoMapping>
            = janzzMappings.flatMap { outer -> outer.classifications.ESCO.map { esco -> CachedEscoMapping(outer.label, outer.conceptId.toString(), esco, ZonedDateTime.now()) } }


    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24)
    fun pruneJanzzCache() {
        log.info("Pruning ESCO JANZZ cache")
        janzzCacheRepository.pruneCache()
    }
}