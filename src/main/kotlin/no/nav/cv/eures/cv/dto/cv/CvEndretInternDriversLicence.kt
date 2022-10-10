package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.arbeid.cv.avro.FoererkortKlasse
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternDriversLicence(
    val klasse: String?,
    val description: String?,
    val acquiredDate: ZonedDateTime?,
    val expiryDate: ZonedDateTime?
)