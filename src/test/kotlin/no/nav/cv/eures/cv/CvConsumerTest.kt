package no.nav.cv.eures.cv

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.any
import io.micrometer.core.instrument.MeterRegistry
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.CvMeldingstype
import no.nav.cv.dto.cv.CvEndretInternCvDto
import no.nav.cv.dto.cv.CvEndretInternLanguage
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.ZonedDateTime
import java.util.*

class CvConsumerTest {

    private lateinit var cvConsumer: CvConsumer


    private val cvRawService = Mockito.mock(CvRawService::class.java)
    private val meterRegistry = Mockito.mock(MeterRegistry::class.java)

    private val testData = CvTestData()

    private val PARTITION = 0

    val stringCaptor = com.nhaarman.mockitokotlin2.argumentCaptor<String>()
    val meldingCaptorCvInternDto = com.nhaarman.mockitokotlin2.argumentCaptor<CvEndretInternDto>()

    val jacksonMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())

    @BeforeEach
    fun setup() {
        cvConsumer = CvConsumer(cvRawService, meterRegistry)
    }

    private fun internRecord(offset: Long, aktorId: String, dto: CvEndretInternDto) = ConsumerRecord<String, String>(
        "\${kafka.aiven.topics.consumers.cv_endret_intern}",
        PARTITION,
        offset,
        aktorId,
        jacksonMapper.writeValueAsString(dto)
    )


    @Test
    fun `test at cv-endret-intern-v3 blir parset korrekt og rutet korrekt til createOrUpdate`() {
        var offset = 0L
        var aktorId = "123"
        var language = "Norsk"
        var meldingsType = CvMeldingstype.OPPRETT
        val cv = createCvEndretInternDto(aktorId, "", language, meldingsType)

        cvConsumer.receive(
            listOf(
                internRecord(offset, testData.aktoerId1, cv)
            )
        )

        Mockito.verify(cvRawService, Mockito.times(1)).createOrUpdateRawCvRecord(meldingCaptorCvInternDto.capture(), any())

        assertEquals(aktorId, meldingCaptorCvInternDto.firstValue.aktorId, "Skal få 123 som aktørId")
        assertEquals(
            language,
            meldingCaptorCvInternDto.firstValue.cv?.languages?.get(0)?.language,
            "Skal få norsk som språk"
        )
    }

    @Test
    fun `test at cv-endret-intern-v3 blir rutet korrekt til cvConverterService2delete`() {

        var offset = 0L
        var fodselsnr = "11111111"
        var aktoerID = "123"
        var meldingsType = CvMeldingstype.SLETT
        var cvEndretInternDto = createCvEndretInternDto(aktoerID, fodselsnr, "", meldingsType)
        cvConsumer.receive(
            listOf(
                internRecord(offset, testData.aktoerId1, cvEndretInternDto)
            )
        )

        Mockito.verify(cvRawService, Mockito.times(1)).deleteCv(stringCaptor.capture())
        assertEquals(aktoerID, stringCaptor.firstValue, "Skal gi fødselsnummeret mottatt i receiveren.")


    }

    private fun createCvEndretInternDto(
        aktorId: String,
        fodselsnr: String,
        language: String,
        meldingstype: CvMeldingstype
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
                createdAt = ZonedDateTime.now(),
                updatedAt = ZonedDateTime.now()
            ),
            personalia = null,
            jobWishes = null,
            oppfolgingsInformasjon = null,
            updatedBy = null
        )
    }



}
