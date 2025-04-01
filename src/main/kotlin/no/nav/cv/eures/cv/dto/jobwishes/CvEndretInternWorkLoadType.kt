package no.nav.cv.dto.jobwishes

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternWorkLoadType(
    val title: Omfang?
)

enum class Omfang {
    HELTID, DELTID;
}
