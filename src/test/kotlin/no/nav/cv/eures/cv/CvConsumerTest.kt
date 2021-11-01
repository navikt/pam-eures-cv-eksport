package no.nav.cv.eures.cv

import com.nhaarman.mockitokotlin2.doReturn
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.arbeid.cv.avro.Melding
import no.nav.cv.eures.samtykke.SamtykkeService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import java.time.ZonedDateTime

class CvConsumerTest {

    private lateinit var cvConsumer : CvConsumer

    private val cvRepository = Mockito.mock(CvRepository::class.java)
    private val samtykkeService = Mockito.mock(SamtykkeService::class.java)


    private val meterRegistry = SimpleMeterRegistry()
    private val testData = CvTestData()

    private val TOPIC = "test-topic"
    private val PARTITION = 0

    val meldingCaptorRawCv = ArgumentCaptor.forClass(RawCV::class.java)
    val stringCaptor = com.nhaarman.mockitokotlin2.argumentCaptor<String>()

    @BeforeEach
    fun setup() {
        cvConsumer = CvConsumer(cvRepository, samtykkeService, meterRegistry)
    }

    @Test
    fun `mottar en og en cv - lagres riktig`() {
        cvConsumer.receive(listOf(record(0, testData.aktoerId1, testData.melding1)))
        cvConsumer.receive(listOf(record(1, testData.aktoerId2, testData.melding2)))

        Mockito.verify(cvRepository, Mockito.times(2)).saveAndFlush(meldingCaptorRawCv.capture())

        assertEquals(testData.foedselsnummer1, meldingCaptorRawCv.allValues[0].foedselsnummer)
        assertEquals(testData.foedselsnummer2, meldingCaptorRawCv.allValues[1].foedselsnummer)
    }

    @Test
    fun `mottar to cver - lagres riktig`() {
        var offset = 0L

        cvConsumer.receive(listOf(
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

        cvConsumer.receive(listOf(
            record(offset++, testData.aktoerId1, testData.meldingMedOppfolgingsinformasjon),
            record(offset, testData.aktoerId2, testData.meldingUtenOppfolgingsinformasjo)
        ))

        Mockito.verify(samtykkeService, Mockito.times(1))
            .slettSamtykke(stringCaptor.capture())

        assertEquals(testData.foedselsnummer2, stringCaptor.firstValue)
    }


    private fun record(offset: Long, aktorId: String, melding: Melding)
    = ConsumerRecord<String, ByteArray>(TOPIC, PARTITION, offset, aktorId, melding.toByteArray())
}