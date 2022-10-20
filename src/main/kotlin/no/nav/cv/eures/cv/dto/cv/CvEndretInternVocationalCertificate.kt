package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.arbeid.cv.avro.Fagdokumentasjon
import no.nav.arbeid.cv.avro.FagdokumentasjonType
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternVocationalCertificate(
    val title: String?,
    val certificateType: String?
) {

    companion object {
        private val logger = LoggerFactory.getLogger(CvEndretInternEducation::class.java)
    }

    fun toAvro(): Fagdokumentasjon =
        Fagdokumentasjon.newBuilder().also {
            it.tittel = title
            it.type = try {
                certificateType?.let { type -> FagdokumentasjonType.valueOf(type) }
            } catch (e: IllegalArgumentException) {
                logger.error("Received certificateType that doesn't exist in Fagdokumentasjon-avro")
                throw e
            }
        }.build()

}