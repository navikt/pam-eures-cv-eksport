package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternEducation(
    val institution: String?,
    val field: String?,
    val nuskode: String?,
    val hasAuthorization: Boolean?,
    val vocationalCollege: String?,
    val startDate: ZonedDateTime?,
    val endDate: ZonedDateTime?,
    val description: String?
)