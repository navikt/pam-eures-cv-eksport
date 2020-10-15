package no.nav.cv.eures.cv

import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeid.cv.avro.Melding
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Inject

// TODO - Fix tests
@MicronautTest
class CvConsumerTest {

    @Inject
    private lateinit var cvConsumer : CvConsumer

    @Inject
    private lateinit var cvRepository: CvRepository

    private lateinit var consumer: MockConsumer<String, ByteArray>

    private val testData = CvTestData()

    private val TOPIC = "test-topic"
    private val PARTITION = 0

    @BeforeEach
    fun setUp() {
        consumer = MockConsumer(OffsetResetStrategy.EARLIEST)

        val startOffsets = HashMap<TopicPartition, Long>()
        val tp = TopicPartition(TOPIC, PARTITION)
        startOffsets[tp] = 0L
        consumer.updateBeginningOffsets(startOffsets)
        consumer.assign(Collections.singleton(TopicPartition(TOPIC, PARTITION)))
    }

    @Test
    fun `mottar en og en cv - lagres riktig`() {
        var offset = 0L

        consumer.schedulePollTask { consumer.addRecord(record(offset++, testData.aktoerId1, testData.melding1)) }
        cvConsumer.process(consumer)

        assertTrue(sjekkAktor(testData.aktoerId1, testData.rawAvro1Base64))

        consumer.addRecord(record(offset++, testData.aktoerId2, testData.melding2))
        cvConsumer.process(consumer)

        assertTrue(sjekkAktor(testData.aktoerId1, testData.rawAvro1Base64))
        assertTrue(sjekkAktor(testData.aktoerId2, testData.rawAvro2Base64))
    }

    @Test
    fun `mottar to cver - lagres riktig`() {
        var offset = 0L

        consumer.schedulePollTask {
            consumer.addRecord(record(offset++, testData.aktoerId1, testData.melding1))
            consumer.addRecord(record(offset++, testData.aktoerId2, testData.melding2))
        }
        cvConsumer.process(consumer)

        assertTrue(sjekkAktor(testData.aktoerId1, testData.rawAvro1Base64))
        assertTrue(sjekkAktor(testData.aktoerId2, testData.rawAvro2Base64))
    }

//    TODO: Finn ut hvorfor denne testen feiler på siste assert
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