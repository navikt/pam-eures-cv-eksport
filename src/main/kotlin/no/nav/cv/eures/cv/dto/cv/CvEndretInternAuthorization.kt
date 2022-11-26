package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.arbeid.cv.avro.Godkjenning
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternAuthorization(
    val title: String?,
    val conceptId: String?,
    val issuer: String?,
    val fromDate: ZonedDateTime?,
    val toDate: ZonedDateTime?
)