package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternCertificate(
    val certificateName: String?,
    val alternativeName: String?,
    val conceptId: String?,
    val issuer: String?,
    val fromDate: ZonedDateTime?,
    val toDate: ZonedDateTime?
)

enum class FagdokumentasjonType {
    SVENNEBREV_FAGBREV, MESTERBREV, AUTORISASJON
}