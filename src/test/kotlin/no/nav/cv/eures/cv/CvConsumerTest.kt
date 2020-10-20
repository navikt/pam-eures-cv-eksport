package no.nav.cv.eures.cv

import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeid.cv.avro.Melding
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class CvConsumerTest {

    @Inject
    private lateinit var cvConsumer : CvConsumer

    @Inject
    private lateinit var cvRepository: CvRepository

    private val testData = CvTestData()

    private val TOPIC = "test-topic"
    private val PARTITION = 0


    @Test
    fun `mottar en og en cv - lagres riktig`() {
        cvConsumer.receive(listOf(record(0, testData.aktoerId1, testData.melding1)))

        assertTrue(sjekkAktor(testData.aktoerId1, testData.rawAvro1Base64))

        cvConsumer.receive(listOf(record(1, testData.aktoerId2, testData.melding2)))

        assertTrue(sjekkAktor(testData.aktoerId1, testData.rawAvro1Base64))
        assertTrue(sjekkAktor(testData.aktoerId2, testData.rawAvro2Base64))
    }

    @Test
    fun `mottar to cver - lagres riktig`() {
        var offset = 0L

        cvConsumer.receive(listOf(
            record(offset++, testData.aktoerId1, testData.melding1),
            record(offset++, testData.aktoerId2, testData.melding2)
        ))

        assertTrue(sjekkAktor(testData.aktoerId1, testData.rawAvro1Base64))
        assertTrue(sjekkAktor(testData.aktoerId2, testData.rawAvro2Base64))
    }

//    @Test
//    fun seekToBegining() {
//        var offset = 0L
//
//        consumer.schedulePollTask {
//            consumer.addRecord(record(offset++, testData.aktorId1, testData.rawAvro1))
//            consumer.addRecord(record(offset++, testData.aktorId2, testData.rawAvro2))
//        }
//
//        val recordsBeforeSeek1 = consumer.poll(Duration.ofSeconds(1))
//
//        assertEquals(2, recordsBeforeSeek1.toList().size)
//
//        val recordsBeforeSeek2 = consumer.poll(Duration.ofSeconds(1))
//
//        assertEquals(0, recordsBeforeSeek2.toList().size)
//
//
//        cvConsumer.seekToBeginningActual(consumer)
//
//        val recordsAfterSeek = consumer.poll(Duration.ofSeconds(1))
//
//        assertEquals(2, recordsAfterSeek.toList().size)
//    }

    private fun sjekkAktor(aktorId: String, rawAvroBase64: String) : Boolean {
        val hentet = cvRepository.hentCvByAktoerId(aktorId)

        return hentet != null
                && hentet.aktoerId == aktorId
                && hentet.rawAvro == rawAvroBase64
    }

    private fun record(offset: Long, aktorId: String, melding: Melding)
    = ConsumerRecord<String, ByteArray>(TOPIC, PARTITION, offset, aktorId, melding.toByteArray())
}