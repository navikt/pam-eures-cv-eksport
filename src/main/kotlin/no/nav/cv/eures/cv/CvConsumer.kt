package no.nav.cv.eures.cv

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
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
        private val cvRepository: CvRepository,
        private val meterRegistry: MeterRegistry
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CvConsumer::class.java)
    }


    @KafkaListener(
            groupId = "pam-eures-cv-eksport-v4",
            topics = ["\${kafka.topics.consumers.cv_endret}"],
            containerFactory = "cvMeldingContainerFactory",
            properties = [
                "auto.offset.reset:earliest"
            ]
    )
    fun receive(record: List<ConsumerRecord<String, ByteArray>>) {
        log.debug("Receiving cv melding message")
        processMessages(record)
    }

    private fun String.foedselsnummerOrNull(): String? {
        if (this == "-") return null

        return this
    }

    /**
     * This function is in charge of three things.
     * 1) If a record exists and the 'underOppfoelging' flag is false it updates the record.
     * 2) If a record exists with the 'underOppfoelging' flag set to true and the update has
     *    no 'oppfolgingsinformasjon', the record is flagged for deletion.
     * 3) If no record exists, it creates a new one.
     */
    private fun Melding.createOrUpdateRawCvRecord(rawAvroBase64: String) {

        val foedselsnummer = extractFoedselsnummer()

        if (foedselsnummer == null) {
            // TODO ta vekk logging av aktørid i prod
            log.warn("Kafkamelding mangler fødselsnummer - hopper over den ($aktoerId) - Meldingstype: $meldingstype. [ endretCv: $endreCv, endretCv.cv: ${endreCv?.cv},  opprettCv: $opprettCv, opprettCv.cv: ${opprettCv?.cv} ]")
            return
        }

        val existing = cvRepository.hentCvByFoedselsnummer(foedselsnummer)

        if(existing != null) {
            if (existing.underOppfoelging && oppfolgingsinformasjon == null) {
                delete()
            } else {
                existing.update(
                        sistEndret = ZonedDateTime.now(),
                        rawAvro = rawAvroBase64,
                        underOppfoelging = (oppfolgingsinformasjon != null),
                        meldingstype = UPDATE
                )
            }
        } else {
            val newRawCv = RawCV.create(
                    aktoerId = aktoerId,
                    foedselsnummer = foedselsnummer,
                    sistEndret = ZonedDateTime.now(),
                    rawAvro = rawAvroBase64,
                    underOppfoelging = (oppfolgingsinformasjon != null),
                    meldingstype = CREATE
            )

            try {
                cvRepository.saveAndFlush(newRawCv)
            } catch (e: Exception) {
                log.error("Fikk exception ${e.message} under lagring av cv $this", e)
            }
        }
    }

    private fun Melding.extractFoedselsnummer() = opprettCv?.cv?.fodselsnummer?.foedselsnummerOrNull()
            ?: endreCv?.cv?.fodselsnummer?.foedselsnummerOrNull()

    private fun Melding.delete(): RawCV? = cvRepository.hentCvByAktoerId(aktoerId)?.update(
            sistEndret = ZonedDateTime.now(),
            underOppfoelging = false,
            meldingstype = DELETE
    )

    private fun Melding.createUpdateOrDelete(rawAvroBase64: String) {
        meterRegistry.counter("cv.endring.mottatt",
                "meldingstype", meldingstype.name)
        log.debug("id: Meldingstype: ${meldingstype.name} - $aktoerId, OpprettCv: $opprettCv, EndreCv: $endreCv, SlettCv: $slettCv, OpprettJobbprofil: $opprettJobbprofil, EndreJobbprofil: $endreJobbprofil, SlettJobbprofil: $slettJobbprofil, Oppfolgingsinformasjon: $oppfolgingsinformasjon, ")
        when (meldingstype) {
            Meldingstype.OPPRETT -> createOrUpdateRawCvRecord(rawAvroBase64)
            Meldingstype.ENDRE -> createOrUpdateRawCvRecord(rawAvroBase64)
            Meldingstype.SLETT -> delete()
            null -> throw Exception("Invalid meldingstype: null")
        }

    }

    private fun ByteArray.toMelding(): Melding {
        val datumReader = SpecificDatumReader(Melding::class.java)
        val decoder = DecoderFactory.get().binaryDecoder(slice(5 until size).toByteArray(), null)
        return datumReader.read(null, decoder)
    }

    private fun processMessages(endretCV: List<ConsumerRecord<String, ByteArray>>) {
        if (endretCV.isNotEmpty()) {
            log.debug("Fikk ${endretCV.size} meldinger.")
        }

        endretCV.forEach { melding ->
            try {

                val meldingValue = melding.value()
                val rawAvroBase64 = Base64.getEncoder().encodeToString(meldingValue)
                meldingValue.toMelding().createUpdateOrDelete(rawAvroBase64)
            } catch (e: Exception) {
                log.error("Klarte ikke behandkle kafkamelding ${melding.key()} (partition: ${melding.partition()} - offset ${melding.offset()} - størrelse: ${melding.value().size}", e)
            }
        }
    }

}
