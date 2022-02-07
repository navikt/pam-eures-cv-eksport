package no.nav.cv.eures.janzz

import com.fasterxml.jackson.module.kotlin.*
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.cv.eures.janzz.dto.CachedEscoMapping
import no.nav.cv.eures.janzz.dto.JanzzEscoLabelMapping
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


@Service
class JanzzService(
    private val client: JanzzQuery,
    private val janzzCacheRepository: JanzzCacheRepository,

    @Value("\${janzz.authorization.token}") private val token: String,
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(JanzzService::class.java)
        private val objectMapper = ObjectMapper().registerModule(KotlinModule())
        private val skillEscoLinkLength = 69
        private val occupationEscoLinkLength = 74
    }

    enum class EscoLookupType {
        OCCUPATION,
        SKILL
    }

    fun getEscoForOccupation(conceptTitle: String): List<CachedEscoMapping> = getEscoForTerm(conceptTitle,
        EscoLookupType.OCCUPATION
    )

    fun getEscoForSkill(term: String): List<CachedEscoMapping> = getEscoForTerm(term, EscoLookupType.SKILL)

    fun getTermForEsco(escoCode: String): String? = janzzCacheRepository.getCacheForEsco(escoCode)

    fun getEscoForTerm(term: String, escoLookup: EscoLookupType): List<CachedEscoMapping> {
        val cachedEsco = janzzCacheRepository.fetchFromCacheTerm(term)

        log.info("Cache for $escoLookup $term contains ${cachedEsco.size} hits")

        return when {
            cachedEsco.isNotEmpty() -> cachedEsco
            else -> fetchAndSaveToCache(term, escoLookup)
        }
    }

    private fun fetchAndSaveToCache(term: String, escoLookupType: EscoLookupType): List<CachedEscoMapping> {
        val queryResult = queryJanzzByTerm(term, escoLookupType)
        janzzCacheRepository.saveToCache(queryResult)
        log.info("Saved ${queryResult.size} results to cache for term $term")

        return queryResult
    }

    private fun queryJanzzByTerm(term: String, escoLookupType: EscoLookupType): List<CachedEscoMapping> {
        val authorization = "token $token"

        val json = client.janzzSearch(
            authorization = authorization,
            query = term,
            branchType = escoLookupType)

        if (json == null) {
            log.error("Janzz query for term $term returned null")
            return listOf(
                CachedEscoMapping(
                term = term,
                conceptId = "",
                esco = "NO HIT",
                updated = ZonedDateTime.now())
            )
        }

        //exact match
        val res = objectMapper.readValue<List<JanzzEscoLabelMapping>>(json).first()
        val escoLinkLength = if (escoLookupType == EscoLookupType.SKILL) skillEscoLinkLength else occupationEscoLinkLength

        val hits = res.classifications.ESCO.filter { esco ->
            esco.length == escoLinkLength
        }.map { esco ->
            CachedEscoMapping(
                term = res.label,
                conceptId = res.conceptId.toString(),
                esco = esco,
                updated = ZonedDateTime.now()
            )
        }

        return hits.ifEmpty {
            log.info("Janzz query for term $term had no valid esco codes")
            listOf(
                CachedEscoMapping(
                term = term,
                conceptId = "",
                esco = "NO HIT",
                updated = ZonedDateTime.now())
            )
        }
    }
}

