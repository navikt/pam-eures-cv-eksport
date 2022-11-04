package no.nav.cv.eures.cv

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.CvMeldingstype
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


@Service
class CvConsumer(
        private val cvRawService: CvRawService
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CvConsumer::class.java)
        val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
    }

    @KafkaListener(
        topics = ["\${kafka.topics.consumers.cv_endret_intern}"],
        containerFactory = "internCvTopicContainerFactory"
    )
    fun receive(record: List<ConsumerRecord<String, String>>) {
        log.debug("Receiving cv message from new topic.")
        processMessages(record)
    }

    private fun processMessages(endretCV: List<ConsumerRecord<String, String>>) {
        log.debug("Fikk ${endretCV.size} meldinger fra CV endret Kafka.")

        endretCV.forEach { melding ->
            log.info("Mottatt json-melding på topic: " + melding.topic())
            processMessage(melding)
        }
    }

    private fun processMessage(endretCV: ConsumerRecord<String, String>) {
        try {
            val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val isoDate = df.format(Date(endretCV.timestamp()))

            log.debug("Processing json kafka message with key ${endretCV.key()} with timestamp $isoDate")
            val cvEndretInternDto = objectMapper.readValue<CvEndretInternDto>(endretCV.value())

            when (cvEndretInternDto.meldingstype) {
                CvMeldingstype.OPPRETT -> cvRawService.createOrUpdateRawCvRecord(cvEndretInternDto)
                CvMeldingstype.ENDRE -> cvRawService.createOrUpdateRawCvRecord(cvEndretInternDto)
                CvMeldingstype.SLETT -> cvRawService.deleteCv(cvEndretInternDto.aktorId)
            }
        } catch (e: Exception) {
            log.warn("Klarte ikke behandle kafkamelding ${endretCV.key()} (partition: ${endretCV.partition()} - offset ${endretCV.offset()}  StackTrace: ${e.stackTraceToString()}", e)
        }
    }

}
