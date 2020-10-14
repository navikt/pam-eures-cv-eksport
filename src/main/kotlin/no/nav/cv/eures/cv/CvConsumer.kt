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

    @Scheduled(fixedDelay = "300ms")
    fun cron() {
        // TODO: Fiks slik at denne ikke kjører under testing

        // log.info("cron() starter")

        process(consumer)
    }

    /**
     *
     */
    private fun Melding.createOrUpdateRawCvRecord(rawAvroBase64: String): RawCV =
            cvRepository.hentCvByAktoerId(aktoerId)?.let { rawCvRecord ->
                if (rawCvRecord.underOppfoelging && oppfolgingsinformasjon == null) {
                    delete()
                } else {
                    rawCvRecord.update(
                        sistEndret = ZonedDateTime.now(),
                        rawAvro = rawAvroBase64,
                        underOppfoelging = (oppfolgingsinformasjon != null),
                        meldingstype = meldingstype
                    )
                }
            }?: RawCV.create(
                    aktoerId = aktoerId,
                    foedselsnummer = opprettCv?.cv?.fodselsnummer ?: endreCv?.cv?.fodselsnummer ?: "-",
                    sistEndret = ZonedDateTime.now(),
                    rawAvro = rawAvroBase64,
                    underOppfoelging = (oppfolgingsinformasjon != null),
                    meldingstype = meldingstype
            )

    private fun Melding.delete(): RawCV? = cvRepository.hentCvByAktoerId(aktoerId)?.update(
            sistEndret = ZonedDateTime.now(),
            rawAvro = "",
            underOppfoelging = false,
            meldingstype = Meldingstype.SLETT
    )

    private fun Melding.toRawCV(rawAvroBase64: String): RawCV? = when (meldingstype) {
        Meldingstype.OPPRETT -> createOrUpdateRawCvRecord(rawAvroBase64)
        Meldingstype.ENDRE -> createOrUpdateRawCvRecord(rawAvroBase64)
        Meldingstype.SLETT -> delete()
        null -> throw Exception("Invalid meldingstype: null")
    }

    private fun ByteArray.toMelding(): Melding {
        val datumReader = SpecificDatumReader<Melding>(Melding::class.java)
        val decoder = DecoderFactory.get().binaryDecoder(slice(5 until size).toByteArray(), null)
        return datumReader.read(null, decoder)
    }

    fun process(consumer: Consumer<String, ByteArray>) {
        val endredeCVer = concurrencyLock.withLock { consumer.poll(Duration.ofSeconds(1)) }

        endredeCVer.count().let {
            if (it > 0) log.info("Fikk $it CVer")
        }

        endredeCVer.forEach { melding ->
            val meldingValue = melding.value()
            val rawAvroBase64 = Base64.getEncoder().encodeToString(meldingValue)
            val rawCV = meldingValue
                    .toMelding()
                    .toRawCV(rawAvroBase64)

            rawCV?.run{
                try {
                    cvRepository.lagreCv(this)
                } catch (e: Exception) {
                    log.error("Fikk exception ${e.message} under lagring av cv $this", e)
                }
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

    private fun createConsumer(): Consumer<String, ByteArray> {
        val props = Properties()
        props["bootstrap.servers"] = bootstrapServers
        props["group.id"] = groupId
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = ByteArrayDeserializer::class.java
        props["max.poll.records"] = 200
        props["fetch.max.bytes"] = 10 * 1024
        val consumer = KafkaConsumer<String, ByteArray>(props)
        consumer.subscribe(listOf(topic))
        return consumer
    }
}
