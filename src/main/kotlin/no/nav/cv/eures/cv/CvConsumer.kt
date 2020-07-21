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
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Singleton
import kotlin.concurrent.withLock

@Singleton
class CvConsumer(
        @Value("\${kafka.bootstrap.servers}") private val bootstrapServers: String,
        @Value("\${kafka.topics.consumers.cv_endret}") private val topic: String,
        @Value("\${kafka.topics.consumers.group_id}") private val groupId: String,
        private val cvRepository: CvRepository
) {

    private val consumer by lazy { createConsumer() }

    private val concurrencyLock = ReentrantLock()

    companion object {
        val log = LoggerFactory.getLogger(CvConsumer::class.java)
    }

    @Scheduled(fixedDelay = "5s")
    fun cron() {
        // TODO: Fiks slik at denne ikke kjører under testing

        log.info("cron() starter")

        process(consumer)
    }

    fun process(consumer: Consumer<String, String>) {
        val endredeCVer = concurrencyLock.withLock { consumer.poll(Duration.ofSeconds(1)) }

        log.info("Fikk ${endredeCVer.count()} CVer")

        for(melding in endredeCVer) {
            val aktorId = melding.key()
            val rawAvroBase64 = Base64.getEncoder().encodeToString(melding.value().toByteArray())

            val oppdatertCv = cvRepository
                    .hentCv(aktorId)
                    ?.update(aktorId = aktorId,
                            sistEndret = ZonedDateTime.now(),
                            rawAvro = rawAvroBase64)
                    ?: RawCV.create(
                            aktorId = aktorId,
                            sistEndret = ZonedDateTime.now(),
                            rawAvro = rawAvroBase64)

            try {
                cvRepository.lagreCv(oppdatertCv)
            } catch (e: Exception) {
                log.error("Fikk exception $e under lagring av cv $oppdatertCv")
            }
        }
    }

    fun seekToBeginning() {
        seekToBeginningActual(consumer)
    }

    fun seekToBeginningActual(consumer: Consumer<String, String>) {
        log.info("Kjører seekToBeginning() på CvConsumer")

        concurrencyLock.withLock {
            // For at seekToBeginning skal fungere må vi ha kjørt poll() minst en gang, siden subscribe er lazy
            // https://stackoverflow.com/questions/41997415/why-calls-to-seektobeginning-and-seektoend-apis-of-kafka-hang-forever
            consumer.poll(Duration.ofSeconds(1))

            // TODO Er dette virkelig starten, eller kun per partisjon? Dokumentasjonen sier at dette søker tilbake til begynnelsen av "partitions your consumer is currently assigned to"
            consumer.seekToBeginning(consumer.assignment())
        }
    }

    private fun createConsumer() : Consumer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = bootstrapServers
        props["group.id"] = groupId
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = StringDeserializer::class.java
        props["max.poll.records"] = 200
        props["fetch.max.bytes"] = 10*1024
        val consumer = KafkaConsumer<String, String>(props)
        consumer.subscribe(listOf(topic))
        return consumer
    }
}