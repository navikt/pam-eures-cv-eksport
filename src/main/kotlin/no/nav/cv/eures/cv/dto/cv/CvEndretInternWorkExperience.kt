package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.arbeid.cv.avro.Arbeidserfaring
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternWorkExperience(
    val employer: String?,
    val jobTitle: String?,
    val alternativeJobTitle: String?,
    val conceptId: String?,
    val location: String?,
    val description: String?,
    val fromDate: ZonedDateTime?,
    val toDate: ZonedDateTime?,
    val styrkkode: String?,
    val ikkeAktueltForFremtiden: Boolean
)