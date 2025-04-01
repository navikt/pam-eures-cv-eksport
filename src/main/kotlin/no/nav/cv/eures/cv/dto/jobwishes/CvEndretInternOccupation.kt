package no.nav.cv.dto.jobwishes

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternOccupation(
    val title: String?,
    val conceptId: Long?
)
