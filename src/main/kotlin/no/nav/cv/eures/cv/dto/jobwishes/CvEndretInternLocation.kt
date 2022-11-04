package no.nav.cv.dto.jobwishes

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternLocation(
    val location: String?,
    val code: String?
)