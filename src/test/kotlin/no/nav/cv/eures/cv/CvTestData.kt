package no.nav.cv.eures.cv

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.CvMeldingstype
import no.nav.cv.dto.cv.CvEndretInternCvDto
import no.nav.cv.dto.cv.CvEndretInternLanguage
import no.nav.cv.dto.oppfolging.CvEndretInternOppfolgingsinformasjonDto
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

val jacksonMapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule()).writer().withDefaultPrettyPrinter()

data class CvTestData(
    val now: ZonedDateTime = ZonedDateTime.now(),
    val yesterday: ZonedDateTime = ZonedDateTime.now().minusDays(1),

    val aktoerId1: String = "123",
    val aktoerId2: String = "321",

    val foedselsnummer1: String = "12345",
    val foedselsnummer2: String = "54321",

    val foedselsdato1: LocalDate = LocalDate.of(1980, 1, 1),
    val foedselsdato2: LocalDate = LocalDate.of(1990, 1, 1),

    var cv1: CvEndretInternDto = createCvEndretInternDto(aktoerId1, foedselsnummer1, "NOR", CvMeldingstype.OPPRETT, ZonedDateTime.now()),
    var cv2: CvEndretInternDto = createCvEndretInternDto(aktoerId2, foedselsnummer2, "NOR", CvMeldingstype.OPPRETT, ZonedDateTime.now().minusDays(1)),

        val foedselsnummerUkjent: String = "ukjent",

    val underOppfoelging: Boolean = false,
    /*
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
     */
    val cv1MedOppfolgningsinformasjon: CvEndretInternDto = cv1.copy(oppfolgingsInformasjon = CvEndretInternOppfolgingsinformasjonDto(formidlingsgruppe = "formidlingsgruppe", fritattKandidatsok = false, hovedmaal = "Bokmal", manuell = false,
    oppfolgingskontor = "NAV Sagene", servicegruppe = "servicegruppe", veileder = "Veilinda", tilretteleggingsbehov = false, erUnderOppfolging = underOppfoelging, veilTilretteleggingsbehov = listOf())),

    val melding1 : CvEndretInternDto = cv1.copy(aktorId = aktoerId1, meldingstype = CvMeldingstype.OPPRETT),
    val melding2 : CvEndretInternDto = cv2.copy(aktorId = aktoerId2, meldingstype = CvMeldingstype.OPPRETT),
    val meldingMedOppfolgingsinformasjon : CvEndretInternDto = cv1MedOppfolgningsinformasjon.copy(aktorId = aktoerId1, meldingstype = CvMeldingstype.OPPRETT),
    val meldingUtenOppfolgingsinformasjo : CvEndretInternDto = melding2,

    val melding1Serialized : String = jacksonMapper.writeValueAsString(melding1),
    val melding2Serialized : String = jacksonMapper.writeValueAsString(melding2),
)

private fun createCvEndretInternDto(
    aktorId: String,
    fodselsnr: String,
    language: String,
    meldingstype: CvMeldingstype,
    created: ZonedDateTime
): CvEndretInternDto {
    return CvEndretInternDto(
        aktorId = aktorId, kandidatNr = null, fodselsnummer = fodselsnr, meldingstype = meldingstype,
        cv = CvEndretInternCvDto(
            uuid = UUID.randomUUID(),
            hasCar = true,
            summary = "Dyktig i jobben",
            languages = listOf(
                CvEndretInternLanguage(
                    language = language,
                    iso3Code = "",
                    oralProficiency = "",
                    writtenProficiency = ""
                )
            ),
            otherExperience = listOf(),
            workExperience = listOf(),
            courses = listOf(),
            certificates = listOf(),
            education = listOf(),
            vocationalCertificates = listOf(),
            authorizations = listOf(),
            driversLicenses = listOf(),
            skillDrafts = listOf(),
            synligForArbeidsgiver = true,
            synligForVeileder = true,
            createdAt = created,
            updatedAt = created
        ),
        personalia = null,
        jobWishes = null,
        oppfolgingsInformasjon = null,
        updatedBy = null
    )
}