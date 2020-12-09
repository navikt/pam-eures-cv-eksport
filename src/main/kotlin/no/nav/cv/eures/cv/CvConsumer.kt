package no.nav.cv.eures.cv

import no.nav.arbeid.cv.avro.Melding
import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.cv.eures.cv.RawCV.Companion.RecordType.*
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*


@Service
class CvConsumer(
        private val cvRepository: CvRepository
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CvConsumer::class.java)
    }


    @KafkaListener(
            groupId = "pam-eures-cv-eksport-v3",
            topics = [ "\${kafka.topics.consumers.cv_endret}" ],
            containerFactory = "cvMeldingContainerFactory",
            properties = [
                "auto.offset.reset:earliest"
            ]
    )
    fun receive(record: List<ConsumerRecord<String, ByteArray>>) {
        log.debug("Receiving cv melding message")
        processMessages(record)
    }

    private fun String?.foedselsnummerOrNull() = this?.let { if (this != "-") this else null }

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
                    foedselsnummer = opprettCv?.cv?.fodselsnummer?.foedselsnummerOrNull()
                            ?: endreCv?.cv?.fodselsnummer?.foedselsnummerOrNull()
                            ?: "AID-$aktoerId",
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
        val datumReader = SpecificDatumReader(Melding::class.java)
        val decoder = DecoderFactory.get().binaryDecoder(slice(5 until size).toByteArray(), null)
        log.info("Decoding to Melding object -> \n\n ${String(slice(5 until size).toByteArray())} \n\n")
        return datumReader.read(null, decoder).also { log.info("Dekoded meldingstype: ${it.meldingstype}") }
    }

    private fun processMessages(endretCV: List<ConsumerRecord<String, ByteArray>>) {
        if (endretCV.isNotEmpty()) {
            log.debug("Fikk ${endretCV.size} meldinger.")
        }


        try {

            endretCV.forEach { melding ->
                log.debug("Behandler melding ${melding.key()} (partition: ${melding.partition()} - offset ${melding.offset()} - størrelse: ${melding.value().size}")
                val meldingValue = melding.value()
                //log.debug("base64 encoder melding")
                val rawAvroBase64 = Base64.getEncoder().encodeToString(meldingValue)
                //log.debug("Konverterer til melding")
                val rawCV = meldingValue
                        .toMelding()
                        .toRawCV(rawAvroBase64)
                log.debug("Lagrer melding")
                rawCV?.run{
                    try {
                        cvRepository.saveAndFlush(this)
                    } catch (e: Exception) {
                        log.error("Fikk exception ${e.message} under lagring av cv $this", e)
                    }
                }
            }
        } catch (e: Throwable) {
            log.error("Feil under konsumering av melding", e)
            throw e
        }
    }

}
