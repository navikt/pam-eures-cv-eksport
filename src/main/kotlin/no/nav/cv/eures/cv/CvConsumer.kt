package no.nav.cv.eures.cv

import io.micronaut.context.annotation.Value
import io.micronaut.scheduling.annotation.Scheduled
import org.slf4j.LoggerFactory
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton

@Singleton
class CvConsumer(
        @Value("\${kafka.brokers.cv_endret}") private val brokers: String,
        @Value("\${kafka.topics.consumers.cv_endret}") private val topic: String,
        @Value("\${kafka.topics.consumers.group_id}") private val groupId: String,
        private val cvRepository: CvRepository
) {

    private val consumer = createConsumer()

    companion object {
        val log = LoggerFactory.getLogger(CvConsumer::class.java)
    }

    @Scheduled(fixedDelay = "15s")
    fun process() {
        log.info("Process() starter")

        val endredeCVer = consumer.poll(Duration.ofSeconds(1))

        log.info("Fikk ${endredeCVer.count()} CVer")

        for(rawAvro in endredeCVer) {
            log.info("Fikk CV $rawAvro")

            val oppdatertCv = cvRepository
                    .hentCv("123")
                    ?.update(aktorId = "123",
                            sistEndret = ZonedDateTime.now(),
                            rawAvro = rawAvro.value())
                    ?: RawCV.create(
                            aktorId = "123",
                            sistEndret = ZonedDateTime.now(),
                            rawAvro = rawAvro.value())

            cvRepository.lagreCv(oppdatertCv)
        }
    }

    fun seekToBeginning() {
        log.info("Kjører seekToBeginning() på CvConsumer")

        // TODO Legg til locking

        // For at seekToBeginning skal fungere må vi ha kjørt poll() minst en gang, siden subscribe er lazy
        // https://stackoverflow.com/questions/41997415/why-calls-to-seektobeginning-and-seektoend-apis-of-kafka-hang-forever
        consumer.poll(Duration.ofSeconds(1))

        // TODO Er dette virkelig starten, eller kun per partisjon? Dokumentasjonen sier at dette søker tilbake til begynnelsen av "partitions your consumer is currently assigned to"
        consumer.seekToBeginning(consumer.assignment())
    }

    private fun createConsumer() : Consumer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["group.id"] = groupId
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = StringDeserializer::class.java
        val consumer = KafkaConsumer<String, String>(props)
        consumer.subscribe(listOf(topic))
        return consumer
    }
}