package no.nav.cv.dto.jobwishes

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternOccupationType(
    val title: Ansettelsesform?
)

enum class Ansettelsesform {
    ENGASJEMENT, FAST, FERIEJOBB, PROSJEKT,
    SELVSTENDIG_NAERINGSDRIVENDE, SESONG,
    VIKARIAT, TRAINEE, LAERLING, ANNET;
}
