package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.arbeid.cv.avro.AnnenErfaring
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternOtherExperience(
    val description: String?,
    val role: String?,
    val fromDate: ZonedDateTime?,
    val toDate: ZonedDateTime?
)