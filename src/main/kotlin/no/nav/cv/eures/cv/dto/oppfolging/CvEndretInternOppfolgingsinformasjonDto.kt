package no.nav.cv.dto.oppfolging

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternOppfolgingsinformasjonDto(
    val formidlingsgruppe: String,
    val fritattKandidatsok: Boolean,
    val hovedmaal: String,
    val manuell: Boolean,
    val oppfolgingskontor: String,
    val servicegruppe: String,
    val veileder: String,
    val tilretteleggingsbehov: Boolean,
    val veilTilretteleggingsbehov: List<String?>?,
    val erUnderOppfolging: Boolean
)