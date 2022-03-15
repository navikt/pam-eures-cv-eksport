package no.nav.cv.eures.janzz

import com.fasterxml.jackson.module.kotlin.*
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.cv.eures.janzz.dto.CachedEscoMapping
import no.nav.cv.eures.janzz.dto.JanzzEscoLabelMapping
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


internal class EscoCode(
    codes: List<String>,
    branch: String
) {
    companion object {
        private const val ISCO_SUBSTRING = "/esco/isco/"
    }

    private val escoSubstring = "/esco/$branch/"
    private val classificationCodes = codes.partition { it.contains(escoSubstring, ignoreCase = true) }.let { codes ->
        codes.first.ifEmpty {
            codes.second.filter { it.contains(ISCO_SUBSTRING, ignoreCase = true) }
        }
    }

    //all esco if there are esco codes if not all isco
    internal fun classificationCodes() = classificationCodes
}
