package no.nav.cv.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.serialization.Serializable
import no.nav.cv.dto.cv.CvEndretInternCvDto
import no.nav.cv.dto.jobwishes.CvEndretInternJobwishesDto
import no.nav.cv.dto.oppfolging.CvEndretInternOppfolgingsinformasjonDto
import no.nav.cv.dto.person.CvEndretInternPersonaliaDto

@JsonIgnoreProperties(ignoreUnknown = true)
@Serializable
data class CvEndretInternDto(
    val aktorId: String,
    val kandidatNr: String?,
    val fodselsnummer: String?,
    val meldingstype: CvMeldingstype,
    val cv: CvEndretInternCvDto?,
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