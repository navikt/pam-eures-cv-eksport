package no.nav.cv.dto.jobwishes

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternWorkDay(
    val title: String?
)
