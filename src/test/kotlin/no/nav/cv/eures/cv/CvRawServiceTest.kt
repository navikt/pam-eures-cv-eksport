package no.nav.cv.eures.cv

import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.CvMeldingstype
import no.nav.cv.dto.cv.CvEndretInternCvDto
import no.nav.cv.eures.samtykke.SamtykkeService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.ZonedDateTime

class CvRawServiceTest {

    lateinit var cvRawService: CvRawService

    private val cvRepository = mock(CvRepository::class.java)
    private val samtykkeService = mock(SamtykkeService::class.java)

    val rawCvCaptor = com.nhaarman.mockitokotlin2.argumentCaptor<RawCV>()

    @BeforeEach
    fun init() {
        cvRawService = CvRawService(cvRepository, samtykkeService)
    }

    @Test
    fun testDeletingRawCvForAktorId() {
        val aktorId = "123"
        var rawCV = createRawCv(aktorId, "1123123")
        `when`(cvRepository.hentCvByAktoerId(aktorId)).thenReturn(rawCV)
        cvRawService.deleteCv(aktorId)
        Mockito.verify(cvRepository, Mockito.times(1)).saveAndFlush(rawCvCaptor.capture())
        assertTrue(rawCvCaptor.firstValue.aktoerId == aktorId, "Sletter på aktørId")
    }

    @Test
    fun testUpdatingCvWhenAlreadyExists() {
        val fnr = "123123123"
        val aktorId = "123"
        val rawJsonCv = "jsonCv"
        val rawCv = createRawCv(aktorId, fnr)
        val dto = createDto(aktorId, fnr)

        `when`(cvRepository.hentCvByFoedselsnummer(fnr)).thenReturn(rawCv)
        cvRawService.createOrUpdateRawCvRecord(dto, rawJsonCv)

        Mockito.verify(cvRepository, Mockito.times(1)).saveAndFlush(rawCvCaptor.capture())
        assertTrue(rawCvCaptor.firstValue.meldingstype == RawCV.Companion.RecordType.UPDATE)
        assertTrue(rawCvCaptor.firstValue.foedselsnummer == fnr)
        assertTrue(rawCvCaptor.firstValue.jsonCv == rawJsonCv)
    }

    @Test
    fun testCreatingCvWhenNoneExists() {
        val fnr = "12121212"
        val aktorId = "231"
        val rawJsonCv = "newCv"
        val dto = createDto(aktorId, fnr)

        `when`(cvRepository.hentCvByFoedselsnummer(fnr)).thenReturn(null)
        cvRawService.createOrUpdateRawCvRecord(dto, rawJsonCv)

        Mockito.verify(cvRepository, Mockito.times(1)).deleteCvByAktorId(aktorId)
        Mockito.verify(cvRepository, Mockito.times(1)).saveAndFlush(rawCvCaptor.capture())
        assertTrue(rawCvCaptor.firstValue.meldingstype == RawCV.Companion.RecordType.CREATE)
        assertTrue(rawCvCaptor.firstValue.jsonCv == rawJsonCv)
    }

    private fun createRawCv(aktorId: String, fnr: String) : RawCV {
        var rawCV = RawCV()
        rawCV.aktoerId = aktorId
        rawCV.foedselsnummer = fnr
        rawCV.meldingstype = RawCV.Companion.RecordType.CREATE
        return rawCV
    }

    private fun createDto(aktoerId : String, fnr : String) : CvEndretInternDto {
        val cv = CvEndretInternCvDto(otherExperience = emptyList(),
            workExperience = emptyList(),
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
        return CvEndretInternDto(aktorId = aktoerId, meldingstype = CvMeldingstype.OPPRETT, cv = cv, fodselsnummer = fnr,
            jobWishes = null, updatedBy = null, kandidatNr = null, personalia = null, oppfolgingsInformasjon = null)
    }
}