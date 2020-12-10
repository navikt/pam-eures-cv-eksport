package no.nav.cv.eures.cv

import no.nav.arbeid.cv.avro.Melding
import no.nav.cv.eures.konverterer.CvAvroSchema
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.apache.avro.SchemaBuilder
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TokenGeneratorConfiguration::class)
class CvConsumerTest {

    private lateinit var cvConsumer : CvConsumer

    @Autowired
    private lateinit var cvRepository: CvRepository

    private val cvAvroSchema: CvAvroSchema = Mockito.mock(CvAvroSchema::class.java)
    private val testData = CvTestData()

    private val TOPIC = "test-topic"
    private val PARTITION = 0

    @BeforeEach
    fun setup() {
        cvConsumer = CvConsumer(cvRepository, cvAvroSchema)
        Mockito.`when`(cvAvroSchema.getSchema(anyObject())).thenReturn(Melding.getClassSchema())
    }

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

    private fun sjekkAktor(aktorId: String, rawAvroBase64: String) : Boolean {
        val hentet = cvRepository.hentCvByAktoerId(aktorId)

        return hentet != null
                && hentet.aktoerId == aktorId
                && hentet.rawAvro == rawAvroBase64
    }

    private fun record(offset: Long, aktorId: String, melding: Melding)
    = ConsumerRecord<String, ByteArray>(TOPIC, PARTITION, offset, aktorId, melding.toByteArray())


    // Kotlin mockito hack
    private fun <T> anyObject(): T = Mockito.anyObject<T>()

}