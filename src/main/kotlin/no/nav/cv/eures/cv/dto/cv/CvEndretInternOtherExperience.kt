package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternOtherExperience(
    val description: String?,
    val role: String?,
    val fromDate: ZonedDateTime?,
    val toDate: ZonedDateTime?
)