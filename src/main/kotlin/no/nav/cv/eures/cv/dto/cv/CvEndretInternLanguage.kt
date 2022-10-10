package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.arbeid.cv.avro.Ferdighetsnivaa
import no.nav.arbeid.cv.avro.Spraakferdighet
import org.slf4j.LoggerFactory

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternLanguage(
    val language: String?,
    val iso3Code: String?,
    val oralProficiency: String,
    val writtenProficiency: String
) {

    companion object {
        private val logger = LoggerFactory.getLogger(CvEndretInternEducation::class.java)
    }

    fun toAvro(): Spraakferdighet =
        Spraakferdighet.newBuilder().also {
            it.iso3kode = iso3Code
            it.muntlig = try {
                Ferdighetsnivaa.valueOf(oralProficiency)
            } catch (e: IllegalArgumentException) {
                logger.error("Received oralProficiency that doesn't exist in Spraakferdighet-avro")
                throw e
            }
            it.skriftlig = try {
                Ferdighetsnivaa.valueOf(writtenProficiency)
            } catch (e: IllegalArgumentException) {
                logger.error("Received writtenProficiency that doesn't exist in Spraakferdighet-avro")
                throw e
            }
            it.spraaknavn = language
        }.build()
}