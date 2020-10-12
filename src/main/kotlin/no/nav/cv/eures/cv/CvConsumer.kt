package no.nav.cv.eures.cv

import io.micronaut.context.annotation.Value
import io.micronaut.scheduling.annotation.Scheduled
import no.nav.arbeid.cv.avro.Melding
import no.nav.arbeid.cv.avro.Meldingstype
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.slf4j.LoggerFactory
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
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
        val log: Logger = LoggerFactory.getLogger(CvConsumer::class.java)
    }

    @Scheduled(fixedDelay = "5s")
    fun cron() {
        // TODO: Fiks slik at denne ikke kjører under testing

        // log.info("cron() starter")

        process(consumer)
    }

    fun process(consumer: Consumer<String, ByteArray>) {
        val endredeCVer = concurrencyLock.withLock { consumer.poll(Duration.ofSeconds(1)) }

        endredeCVer.count().let {
            if (it > 0) log.info("Fikk $it CVer")
        }

        for(melding in endredeCVer) {
            val aktoerId = melding.key()
            val meldingValue = melding.value()

            val datumReader = SpecificDatumReader<Melding>(Melding::class.java)
            val rawAvroBase64 = Base64.getEncoder().encodeToString(meldingValue)
            val decoder = DecoderFactory.get().binaryDecoder(meldingValue, null)

            val cvMelding = datumReader.read(null, decoder).let {
                when(it.meldingstype) {
                    Meldingstype.OPPRETT -> it.opprettCv.cv
                    Meldingstype.ENDRE -> it.endreCv.cv
                    else -> null
                }
            } ?: return run {
                cvRepository.hentCvByAktoerId(aktoerId)
                    ?.update(sistEndret = ZonedDateTime.now(), rawAvro = "")
                    ?.let {
                        try {
                            cvRepository.lagreCv(it)
                        } catch (e: Exception) {
                            log.error("Fikk exception $e under lagring av slettet cv $it")
                        }
                    }
            }


            val oppdatertCv = cvRepository
                    .hentCvByFoedselsnummer(cvMelding.fodselsnummer)
                    ?.update(
                            aktoerId = aktoerId,
                            foedselsnummer = cvMelding.fodselsnummer,
                            sistEndret = ZonedDateTime.now(),
                            rawAvro = rawAvroBase64)
                    ?: RawCV.create(
                            aktoerId = aktoerId,
                            foedselsnummer = cvMelding.fodselsnummer,
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

    fun seekToBeginningActual(consumer: Consumer<String, ByteArray>) {
        log.info("Kjører seekToBeginning() på CvConsumer")

        concurrencyLock.withLock {
            // For at seekToBeginning skal fungere må vi ha kjørt poll() minst en gang, siden subscribe er lazy
            // https://stackoverflow.com/questions/41997415/why-calls-to-seektobeginning-and-seektoend-apis-of-kafka-hang-forever
            consumer.poll(Duration.ofSeconds(1))

            // TODO Er dette virkelig starten, eller kun per partisjon? Dokumentasjonen sier at dette søker tilbake til begynnelsen av "partitions your consumer is currently assigned to"
            consumer.seekToBeginning(consumer.assignment())
        }
    }

    private fun createConsumer() : Consumer<String, ByteArray> {
        val props = Properties()
        props["bootstrap.servers"] = bootstrapServers
        props["group.id"] = groupId
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = ByteArrayDeserializer::class.java
        props["max.poll.records"] = 200
        props["fetch.max.bytes"] = 10*1024
        val consumer = KafkaConsumer<String, ByteArray>(props)
        consumer.subscribe(listOf(topic))
        return consumer
    }
}
