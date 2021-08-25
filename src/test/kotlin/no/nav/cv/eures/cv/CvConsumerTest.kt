package no.nav.cv.eures.cv

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.arbeid.cv.avro.Melding
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

class CvConsumerTest {

    private lateinit var cvConsumer : CvConsumer

    private val cvRepository = Mockito.mock(CvRepository::class.java)

    private val meterRegistry = SimpleMeterRegistry()
    private val testData = CvTestData()

    private val TOPIC = "test-topic"
    private val PARTITION = 0

    val meldingCaptor = ArgumentCaptor.forClass(RawCV::class.java)

    @BeforeEach
    fun setup() {
        cvConsumer = CvConsumer(cvRepository, meterRegistry)
    }

    @Test
    fun `mottar en og en cv - lagres riktig`() {
        cvConsumer.receive(listOf(record(0, testData.aktoerId1, testData.melding1)))
        cvConsumer.receive(listOf(record(1, testData.aktoerId2, testData.melding2)))

        Mockito.verify(cvRepository, Mockito.times(2)).saveAndFlush(meldingCaptor.capture())

        assertEquals(testData.foedselsnummer1, meldingCaptor.allValues[0].foedselsnummer)
        assertEquals(testData.foedselsnummer2, meldingCaptor.allValues[1].foedselsnummer)
    }

    @Test
    fun `mottar to cver - lagres riktig`() {
        var offset = 0L

        cvConsumer.receive(listOf(
            record(offset++, testData.aktoerId1, testData.melding1),
            record(offset, testData.aktoerId2, testData.melding2)
        ))

        Mockito.verify(cvRepository, Mockito.times(2)).saveAndFlush(meldingCaptor.capture())

        assertEquals(testData.foedselsnummer1, meldingCaptor.allValues[0].foedselsnummer)
        assertEquals(testData.foedselsnummer2, meldingCaptor.allValues[1].foedselsnummer)
    }

    private fun record(offset: Long, aktorId: String, melding: Melding)
    = ConsumerRecord<String, ByteArray>(TOPIC, PARTITION, offset, aktorId, ByteArray(2) + melding.toByteArray())


}