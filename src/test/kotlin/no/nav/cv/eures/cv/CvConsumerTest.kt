package no.nav.cv.eures.cv

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.doReturn
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.arbeid.cv.avro.Melding
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.CvMeldingstype
import no.nav.cv.dto.cv.CvEndretInternCvDto
import no.nav.cv.dto.cv.CvEndretInternLanguage
import no.nav.cv.eures.cv.consumer.CvConsumer
import no.nav.cv.eures.konverterer.CvConverterService2
import no.nav.cv.eures.samtykke.SamtykkeService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import java.time.ZonedDateTime
import java.util.UUID

class CvConsumerTest {

    private lateinit var cvConsumer : CvConsumer

    private val cvRepository = Mockito.mock(CvRepository::class.java)
    private val samtykkeService = Mockito.mock(SamtykkeService::class.java)
    private val cvConverterService2 = Mockito.mock(CvConverterService2::class.java)

    private val meterRegistry = SimpleMeterRegistry()
    private val testData = CvTestData()

    private val TOPIC = "test-topic"
    private val PARTITION = 0

    val meldingCaptorRawCv = ArgumentCaptor.forClass(RawCV::class.java)
    val stringCaptor = com.nhaarman.mockitokotlin2.argumentCaptor<String>()
    val meldingCaptorCvInternDto = com.nhaarman.mockitokotlin2.argumentCaptor<CvEndretInternDto>()

    val jacksonMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())

    @BeforeEach
    fun setup() {
        cvConsumer = CvConsumer(cvRepository, samtykkeService, meterRegistry, cvConverterService2, false)
    }

    @Test
    fun `mottar en og en cv - lagres riktig`() {
        cvConsumer.receiveAvro(listOf(record(0, testData.aktoerId1, testData.melding1)))
        cvConsumer.receiveAvro(listOf(record(1, testData.aktoerId2, testData.melding2)))

        Mockito.verify(cvRepository, Mockito.times(2)).saveAndFlush(meldingCaptorRawCv.capture())

        assertEquals(testData.foedselsnummer1, meldingCaptorRawCv.allValues[0].foedselsnummer)
        assertEquals(testData.foedselsnummer2, meldingCaptorRawCv.allValues[1].foedselsnummer)
    }

    @Test
    fun `mottar to cver - lagres riktig`() {
        var offset = 0L

        cvConsumer.receiveAvro(listOf(
            record(offset++, testData.aktoerId1, testData.melding1),
            record(offset, testData.aktoerId2, testData.melding2)
        ))

        Mockito.verify(cvRepository, Mockito.times(2)).saveAndFlush(meldingCaptorRawCv.capture())

        assertEquals(testData.foedselsnummer1, meldingCaptorRawCv.allValues[0].foedselsnummer)
        assertEquals(testData.foedselsnummer2, meldingCaptorRawCv.allValues[1].foedselsnummer)
    }


    @Test
    fun `mottar avsluttet oppfølging - sletter xml cv og samtykke`() {
        var offset = 0L

        doReturn(
            RawCV.create(
                aktoerId = testData.aktoerId1,
                foedselsnummer = testData.foedselsnummer1,
                sistEndret = ZonedDateTime.now(),
                rawAvro = "",
                underOppfoelging = true,
                meldingstype = RawCV.Companion.RecordType.UPDATE))
            .`when`(cvRepository)
            .hentCvByFoedselsnummer(testData.foedselsnummer1)

        doReturn(
            RawCV.create(
                aktoerId = testData.aktoerId2,
                foedselsnummer = testData.foedselsnummer2,
                sistEndret = ZonedDateTime.now(),
                rawAvro = "",
                underOppfoelging = true,
                meldingstype = RawCV.Companion.RecordType.UPDATE))
            .`when`(cvRepository)
            .hentCvByFoedselsnummer(testData.foedselsnummer2)

        cvConsumer.receiveAvro(listOf(
            record(offset++, testData.aktoerId1, testData.meldingMedOppfolgingsinformasjon),
            record(offset, testData.aktoerId2, testData.meldingUtenOppfolgingsinformasjo)
        ))

        Mockito.verify(samtykkeService, Mockito.times(1))
            .slettSamtykke(stringCaptor.capture())

        assertEquals(testData.foedselsnummer2, stringCaptor.firstValue)
    }

    private fun record(offset: Long, aktorId: String, melding: Melding)
    = ConsumerRecord<String, ByteArray>(TOPIC, PARTITION, offset, aktorId, melding.toByteArray())

    @Disabled
    @Test
    fun `test at cv-endret-intern-v3 blir parset korrekt og rutet korrekt til createOrUpdate`() {
        var offset = 0L
        var aktorId = "123"
        var language = "Norsk"
        var meldingsType = CvMeldingstype.OPPRETT
        cvConsumer.receiveJson(listOf(
            internRecord(offset, testData.aktoerId1, createCvEndretInternDto(aktorId, "", language, meldingsType))))

        Mockito.verify(cvConverterService2, Mockito.times(1)).createOrUpdate(meldingCaptorCvInternDto.capture())

        assertEquals(aktorId, meldingCaptorCvInternDto.firstValue.aktorId, "Skal få 123 som aktørId")
        assertEquals(language, meldingCaptorCvInternDto.firstValue.cv?.languages?.get(0)?.language, "Skal få norsk som språk")
    }

    @Disabled
    @Test
    fun `test at cv-endret-intern-v3 blir rutet korrekt til cvConverterService2delete`() {
        var offset = 0L
        var fodselsnr = "11111111"
        var meldingsType = CvMeldingstype.SLETT
        var cvEndretInternDto = createCvEndretInternDto("", fodselsnr, "", meldingsType)
        cvConsumer.receiveJson(listOf(
            internRecord(offset, testData.aktoerId1, cvEndretInternDto)))

        Mockito.verify(cvConverterService2, Mockito.times(1)).delete(stringCaptor.capture())
        assertEquals(fodselsnr, stringCaptor.firstValue, "Skal gi fødselsnummeret mottatt i receiveren.")
    }

    private fun createCvEndretInternDto(aktorId: String, fodselsnr: String, language: String, meldingstype: CvMeldingstype) : CvEndretInternDto {
        return CvEndretInternDto(aktorId = aktorId, kandidatNr = null, fodselsnummer = fodselsnr, meldingstype = meldingstype,
            cv = CvEndretInternCvDto(
                uuid = UUID.randomUUID(),
                hasCar = true,
                summary = "Dyktig i jobben",
                languages = listOf(CvEndretInternLanguage(
                    language = language,
                    iso3Code = "",
                    oralProficiency = "",
                    writtenProficiency = ""
                )),
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

    private fun internRecord(offset: Long, aktorId: String, dto: CvEndretInternDto)
    = ConsumerRecord<String, ByteArray>("\${kafka.aiven.topics.consumers.cv_endret}", PARTITION, offset, aktorId, jacksonMapper.writeValueAsBytes(dto))

}