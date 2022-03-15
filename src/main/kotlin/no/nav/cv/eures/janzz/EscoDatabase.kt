package no.nav.cv.eures.janzz

import org.springframework.stereotype.Service

@Service
class EscoDatabase(
    private val escoCache: EscoCache
) {
    fun escoTerm(escoCode: String): String? = escoCache.getCacheForEsco(escoCode)

    fun escoCode(escoTerm: String): String



}