package no.nav.cv.eures.cv

import io.micronaut.configuration.kafka.ConsumerAware
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.annotation.Value
import no.nav.arbeid.cv.avro.Melding
import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.cv.eures.cv.RawCV.Companion.RecordType.*
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.*

@KafkaListener(
        groupId = "pam-eures-cv-eksport-testing-01",
        offsetReset = OffsetReset.EARLIEST,
        batch = true
)
class CvConsumer(
        private val cvRepository: CvRepository
) : ConsumerRebalanceListener, ConsumerAware<String, ByteArray> {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CvConsumer::class.java)
    }

    @Value("\${kafka.reset-offset}")
    private lateinit var resetKafkaOffset: String
    lateinit var consumer: Consumer<String, ByteArray>
    private var partitions: MutableCollection<TopicPartition>? = null

    fun seekToBeginning() = consumer.seekToBeginning(partitions)

    override fun onPartitionsRevoked(partitions: MutableCollection<TopicPartition>?) {
        TODO("Not yet implemented")
    }

    override fun onPartitionsAssigned(partitions: MutableCollection<TopicPartition>?) {
        this.partitions = partitions
        if (resetKafkaOffset.toBoolean()) {
            consumer.seekToBeginning(partitions)
        }
    }

    override fun setKafkaConsumer(consumer: Consumer<String, ByteArray>) {
        this.consumer = consumer
    }

    @Topic("\${kafka.topics.consumers.cv_endret}")
    fun receive(
            record: List<ConsumerRecord<String, ByteArray>>
    ) {
        processMessages(record)
    }

    /**
     * This function is in charge of three things.
     * 1) If a record exists and the 'underOppfoelging' flag is false it updates the record.
     * 2) If a record exists with the 'underOppfoelging' flag set to true and the update has
     *    no 'oppfolgingsinformasjon', the record is flagged for deletion.
     * 3) If no record exists, it creates a new one.
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
                        meldingstype = UPDATE
                    )
                }
            }?: RawCV.create(
                    aktoerId = aktoerId,
                    foedselsnummer = opprettCv?.cv?.fodselsnummer ?: endreCv?.cv?.fodselsnummer ?: "-",
                    sistEndret = ZonedDateTime.now(),
                    rawAvro = rawAvroBase64,
                    underOppfoelging = (oppfolgingsinformasjon != null),
                    meldingstype = CREATE
            )

    private fun Melding.delete(): RawCV? = cvRepository.hentCvByAktoerId(aktoerId)?.update(
            sistEndret = ZonedDateTime.now(),
            underOppfoelging = false,
            meldingstype = DELETE
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

    private fun processMessages(endretCV: List<ConsumerRecord<String, ByteArray>>) {
        if (endretCV.isNotEmpty()) {
            log.debug("Fikk ${endretCV.size} meldinger.")
        }


        endretCV.forEach { melding ->
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

}
