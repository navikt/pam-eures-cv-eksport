package no.nav.cv.eures.cv

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.CvMeldingstype
import no.nav.cv.dto.cv.CvEndretInternCvDto
import no.nav.cv.dto.cv.CvEndretInternWorkExperience
import no.nav.cv.dto.oppfolging.CvEndretInternOppfolgingsinformasjonDto
import no.nav.cv.eures.samtykke.SamtykkeService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime

@SpringBootTest
@EnableMockOAuth2Server
class CvRawServiceIntegrationTest {
    lateinit var cvRawService: CvRawService

    @Autowired
    lateinit var cvRepository: CvRepository

    private val samtykkeService = mock(SamtykkeService::class.java)

    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @BeforeEach
    fun init() {
        cvRawService = CvRawService(cvRepository, samtykkeService)
    }

    @Test
    fun `Endringer lagres selv når bruker går fra å ha vært under oppfølging til å ikke være det lenger, men samtykket slettes`() {
        val aktørId = "1".repeat(13)
        val fødselsnummer = "1".repeat(11)

        val cv1 = createDto(aktørId, fødselsnummer, emptyList(), createOppfølgingsinformasjon(true), CvMeldingstype.OPPRETT)

        cvRawService.createOrUpdateRawCvRecord(cv1, objectMapper.writeValueAsString(cv1))

        val cv1FraDatabase = cvRepository.hentCvByFoedselsnummer(fødselsnummer)
        val cv1Dto = objectMapper.readValue<CvEndretInternDto>(cv1FraDatabase?.jsonCv!!)

        assertEquals(0, cv1Dto.cv?.workExperience?.size)
        assertTrue(cv1FraDatabase.underOppfoelging)
        assertTrue(cv1Dto.oppfolgingsInformasjon?.erUnderOppfolging ?: false)
        Mockito.verify(samtykkeService, Mockito.never()).slettSamtykke(fødselsnummer)

        val cv2 = createDto(aktørId, fødselsnummer, listOf(createWorkExperience("Test AS", "Tester")), createOppfølgingsinformasjon(false), CvMeldingstype.ENDRE)
        cvRawService.createOrUpdateRawCvRecord(cv2, objectMapper.writeValueAsString(cv2))

        val cv2FraDatabase = cvRepository.hentCvByFoedselsnummer(fødselsnummer)
        val cv2Dto = objectMapper.readValue<CvEndretInternDto>(cv2FraDatabase?.jsonCv!!)

        assertNotEquals(cv1FraDatabase.sistEndret, cv2FraDatabase.sistEndret)

        assertEquals(1, cv2Dto.cv?.workExperience?.size)
        assertFalse(cv2Dto.oppfolgingsInformasjon?.erUnderOppfolging ?: false)
        assertFalse(cv2FraDatabase.underOppfoelging)
        Mockito.verify(samtykkeService, Mockito.times(1)).slettSamtykke(fødselsnummer)
    }

    private fun createOppfølgingsinformasjon(underOppfølging: Boolean = false) =
        CvEndretInternOppfolgingsinformasjonDto(
            formidlingsgruppe = "",
            fritattKandidatsok = false,
            hovedmaal = "",
            manuell = false,
            oppfolgingskontor = "",
            servicegruppe = "",
            veileder = "",
            tilretteleggingsbehov = false,
            veilTilretteleggingsbehov = emptyList(),
            erUnderOppfolging = underOppfølging
        )

    private fun createWorkExperience(employer: String = "Test AS", jobTitle: String = "Tester") =
        CvEndretInternWorkExperience(
            employer = employer,
            jobTitle = jobTitle,
            alternativeJobTitle = null,
            conceptId = null,
            location = null,
            description = null,
            fromDate = null,
            toDate = null,
            styrkkode = null,
            ikkeAktueltForFremtiden = false
        )

    private fun createDto(
        aktørId: String,
        fnr: String,
        workExperience: List<CvEndretInternWorkExperience> = emptyList(),
        oppfølgingsinformasjon: CvEndretInternOppfolgingsinformasjonDto? = null,
        meldingstype: CvMeldingstype = CvMeldingstype.OPPRETT
    ): CvEndretInternDto {
        val cv = CvEndretInternCvDto(
            otherExperience = emptyList(),
            workExperience = workExperience,
            courses = emptyList(),
            certificates = emptyList(),
            vocationalCertificates = emptyList(),
            education = emptyList(),
            driversLicenses = emptyList(),
            authorizations = emptyList(),
            languages = emptyList(),
            skillDrafts = emptyList(),
            synligForVeileder = false,
            synligForArbeidsgiver = false,
            hasCar = true,
            uuid = null,
            updatedAt = ZonedDateTime.now(),
            createdAt = ZonedDateTime.now(),
            summary = null
        )
        return CvEndretInternDto(
            aktorId = aktørId,
            meldingstype = meldingstype,
            cv = cv,
            fodselsnummer = fnr,
            jobWishes = null,
            updatedBy = null,
            kandidatNr = null,
            personalia = null,
            oppfolgingsInformasjon = oppfølgingsinformasjon
        )
    }
}
