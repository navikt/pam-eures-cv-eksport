package no.nav.cv.eures.cv

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import no.nav.arbeid.cv.avro.Melding
import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.CvMeldingstype
import no.nav.cv.eures.cv.RawCV.Companion.RecordType.*
import no.nav.cv.eures.konverterer.CvConverterService2
import no.nav.cv.eures.samtykke.SamtykkeService
import no.nav.cv.eures.util.toMelding
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*


@Service
class CvConsumer(
    private val cvRepository: CvRepository,
    private val samtykkeService: SamtykkeService,
    private val meterRegistry: MeterRegistry,
    private val cvConverterService2: CvConverterService2,
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CvConsumer::class.java)
        val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule()).setTimeZone(TimeZone.getTimeZone("Europe/Paris"))
    }


    @KafkaListener(
        topics = ["\${kafka.topics.consumers.cv_endret}"],
        containerFactory = "cvMeldingContainerFactory",
    )
    fun receiveAvro(record: List<ConsumerRecord<String, ByteArray>>) {
        log.debug("Receiving cv avro message")
        processAvroMessages(record)
    }

    @KafkaListener(
        topics = ["\${kafka.topics.consumers.cv_endret_json}"],
        containerFactory = "internCvTopicContainerFactory"
    )
    fun receiveJson(record: List<ConsumerRecord<String, String>>) {
        log.debug("Receiving cv json message")
        //processJsonMessages(record)
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
            log.warn("Kafkamelding mangler fødselsnummer - hopper over den ($aktoerId) - Meldingstype: $meldingstype.")
            return
        }

        val existing = cvRepository.hentCvByFoedselsnummer(foedselsnummer)

        if(existing != null) {
            if (existing.underOppfoelging && oppfolgingsinformasjon == null) {
                log.debug("Deleting ${existing.aktoerId} due to not being 'under oppfølging' anymore")

                // By deleting samtykke we also mark the XML CV for deletion
                try {
                    samtykkeService.slettSamtykke(foedselsnummer)
                } catch (e: Exception) {
                    log.error("Fikk exception ${e.message} under sletting av cv $this", e)
                }
            } else {
                log.debug("Updating ${existing.aktoerId}")
                existing.update(
                    sistEndret = ZonedDateTime.now(),
                    rawAvro = rawAvroBase64,
                    underOppfoelging = (oppfolgingsinformasjon != null),
                    meldingstype = UPDATE
                )

                try {
                    cvRepository.saveAndFlush(existing)
                } catch (e: Exception) {
                    log.error("Fikk exception ${e.message} under oppdatring av cv $this", e)
                }
            }
        } else {
            log.debug("inserting new record for $aktoerId")

            cvRepository.deleteCvByAktorId(aktoerId)

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
        meldingstype = DELETE)
        ?.let { cvRepository.saveAndFlush(it) }

    private fun Melding.createUpdateOrDelete(rawAvroBase64: String) {
        meterRegistry.counter("cv.endring.mottatt",
            "meldingstype", meldingstype.name)
        log.debug("id: Meldingstype: ${meldingstype.name} - $aktoerId") //, OpprettCv: $opprettCv, EndreCv: $endreCv, SlettCv: $slettCv, OpprettJobbprofil: $opprettJobbprofil, EndreJobbprofil: $endreJobbprofil, SlettJobbprofil: $slettJobbprofil, Oppfolgingsinformasjon: $oppfolgingsinformasjon, ")
        when (meldingstype) {
            Meldingstype.OPPRETT -> createOrUpdateRawCvRecord(rawAvroBase64)
            Meldingstype.ENDRE -> createOrUpdateRawCvRecord(rawAvroBase64)
            Meldingstype.SLETT -> delete()
            null -> throw Exception("Invalid meldingstype: null")
        }
    }


    private fun processAvroMessages(endretCV: List<ConsumerRecord<String, ByteArray>>) {
        log.debug("Fikk ${endretCV.size} meldinger fra CV endret Kafka.")

        endretCV.forEach { melding ->
            log.info("Mottatt avro-melding på topic: " + melding.topic())
            processAvroMessage(melding)
        }
    }

    private fun processJsonMessages(endretCV: List<ConsumerRecord<String, String>>) {
        log.debug("Fikk ${endretCV.size} meldinger fra CV endret Kafka.")

        endretCV.forEach { melding ->
            log.info("Mottatt json-melding på topic: " + melding.topic())
            processJsonMessage(melding)
        }
    }

    private fun processJsonMessage(endretCV: ConsumerRecord<String, String>) {
        try {
            val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val isoDate = df.format(Date(endretCV.timestamp()))

            log.debug("Processing json kafka message with key ${endretCV.key()} with timestamp $isoDate")
            val cvEndretInternDto = objectMapper.readValue<CvEndretInternDto>(endretCV.value())

            when (cvEndretInternDto.meldingstype) {
                CvMeldingstype.OPPRETT -> cvConverterService2.createOrUpdate(cvEndretInternDto)
                CvMeldingstype.ENDRE -> cvConverterService2.createOrUpdate(cvEndretInternDto)
                CvMeldingstype.SLETT -> cvEndretInternDto.fodselsnummer?.let { cvConverterService2.delete(cvEndretInternDto.fodselsnummer) }
            }
        } catch (e: Exception) {
            log.warn("Klarte ikke behandle kafkamelding ${endretCV.key()} (partition: ${endretCV.partition()} - offset ${endretCV.offset()}  StackTrace: ${e.stackTraceToString()}", e)
        }
    }

    private fun processAvroMessage(endretCV: ConsumerRecord<String, ByteArray>) {
        try {
            val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val isoDate = df.format(Date(endretCV.timestamp()))

            log.debug("Processing avro kafka message with key ${endretCV.key()} with timestamp $isoDate")
            val meldingValue = endretCV.value()
            val rawAvroBase64 = Base64.getEncoder().encodeToString(meldingValue)
            meldingValue.toMelding(endretCV.key()).createUpdateOrDelete(rawAvroBase64)

        } catch (e: Exception) {
            log.warn("Klarte ikke behandle kafkamelding ${endretCV.key()} (partition: ${endretCV.partition()} - offset ${endretCV.offset()} - størrelse: ${endretCV.value().size}  StackTrace: ${e.stackTraceToString()}", e)
        }
    }

}
