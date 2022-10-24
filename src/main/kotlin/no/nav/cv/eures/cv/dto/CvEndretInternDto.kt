package no.nav.cv.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.cv.dto.cv.CvEndretInternCvDto
import no.nav.cv.dto.jobwishes.CvEndretInternJobwishesDto
import no.nav.cv.dto.oppfolging.CvEndretInternOppfolgingsinformasjonDto
import no.nav.cv.dto.person.CvEndretInternPersonaliaDto

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternDto(
    val aktorId: String,
    val kandidatNr: String?,
    val fodselsnummer: String,
    val meldingstype: CvMeldingstype,
    val cv: CvEndretInternCvDto?,
    //cant be null as Person in CV which this field is based on cant be null for cv-endret-intern messages
    val personalia: CvEndretInternPersonaliaDto?,
    val jobWishes: CvEndretInternJobwishesDto?,
    val oppfolgingsInformasjon: CvEndretInternOppfolgingsinformasjonDto?,
    val updatedBy: UpdatedByType?
)

enum class CvMeldingstype {
    OPPRETT, ENDRE, SLETT
}

enum class UpdatedByType {
    PERSONBRUKER, VEILEDER, SYSTEMBRUKER, SYSTEMINTERN
}