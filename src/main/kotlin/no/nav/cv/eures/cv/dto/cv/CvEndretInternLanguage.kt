package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternLanguage(
    val language: String?,
    val iso3Code: String?,
    val oralProficiency: String,
    val writtenProficiency: String
)

enum class Ferdighetsnivaa {
    IKKE_OPPGITT, NYBEGYNNER, GODT, VELDIG_GODT,
    FOERSTESPRAAK
}
