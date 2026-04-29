package no.nav.cv.eures.scheduled

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class EuresSamtykkePayload(
    val antallSamtykker: Long,
    val antallPerLand: Map<String, Int>,
)

data class MetricSinkMetrikk(
    val type: String,
    val payload: String,
    val timestamp: String,
)

@Profile("!test")
@Service
class GenerateMetricSinkMetrics(
    private val samtykkeRepository: SamtykkeRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${kafka.topics.producers.metric_sink}") private val metricSinkTopic: String,
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(GenerateMetricSinkMetrics::class.java)
    }

    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    @Scheduled(cron = "0 0 4 * * *")
    fun generateAndSendMetrics() {
        try {
            val count = samtykkeRepository.hentAntallSamtykkerMedBooleanTrue()

            val countryCounts = samtykkeRepository
                .hentAlleLand().flatMap { json -> objectMapper.readValue<List<String>>(json) }
                .groupingBy { it }
                .eachCount()

            val payload = EuresSamtykkePayload(
                antallSamtykker = count,
                antallPerLand = countryCounts,
            )

            val metrikk = MetricSinkMetrikk(
                type = "EURES_SAMTYKKE",
                payload = objectMapper.writeValueAsString(payload),
                timestamp = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            )

            log.info("Sending metric-sink metric: $count samtykker, country counts $countryCounts")

            kafkaTemplate.send(metricSinkTopic, objectMapper.writeValueAsString(metrikk))
                .whenComplete { result, ex ->
                    if (ex != null) {
                        log.error("Failed to send EURES_SAMTYKKE metric to metric-sink", ex)
                    } else {
                        log.info("Sent EURES_SAMTYKKE metric to metric-sink (offset=${result.recordMetadata.offset()})")
                    }
                }
        } catch (e: Exception) {
            log.error("Error while generating metrics for metric-sink", e)
        }
    }
}
