package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternVocationalCertificate(
    val title: String?,
    val certificateType: String?
)