package no.nav.cv.eures.scheduled

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate

import java.util.concurrent.CompletableFuture

class GenerateMetricSinkMetricsTest {

    private val samtykkeRepository = mock(SamtykkeRepository::class.java)

    @Suppress("UNCHECKED_CAST")
    private val kafkaTemplate = mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>

    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    private lateinit var generateMetricSinkMetrics: GenerateMetricSinkMetrics

    private val topic = "teampam.metric-sink-v1"

    @BeforeEach
    fun setup() {
        generateMetricSinkMetrics = GenerateMetricSinkMetrics(
                samtykkeRepository,
                kafkaTemplate,
                topic,
        )
    }

    @Test
    fun `should send single kafka message with samtykke count and country counts`() {
        whenever(samtykkeRepository.hentAntallSamtykkerMedBooleanTrue()).thenReturn(42L)
        whenever(samtykkeRepository.hentAlleLand()).thenReturn(listOf(
                """["NO","SE"]""",
                """["NO","DE"]""",
                """["NO"]""",
        ))

        val valueCaptor = argumentCaptor<String>()
        whenever(kafkaTemplate.send(eq(topic), any(), valueCaptor.capture()))
                .thenReturn(CompletableFuture())

        generateMetricSinkMetrics.generateAndSendMetrics()

        verify(kafkaTemplate, times(1)).send(eq(topic), any(), any())

        val sentJson = valueCaptor.firstValue
        val metrikk = objectMapper.readValue<MetricSinkMetrikk>(sentJson)

        assertEquals("EURES_CV_EKSPORT", metrikk.type)
        assertNotNull(metrikk.timestamp)
        assertFalse(metrikk.timestamp.contains("Z"), "Timestamp should be LocalDateTime format without timezone")

        val payload = objectMapper.readValue<EuresCvEksportMetrikkPayload>(metrikk.payload)
        assertEquals(42L, payload.antallSamtykker)
        assertEquals(3, payload.antallPerLand["NO"])
        assertEquals(1, payload.antallPerLand["SE"])
        assertEquals(1, payload.antallPerLand["DE"])
    }

    @Test
    fun `should handle empty land list`() {
        whenever(samtykkeRepository.hentAntallSamtykkerMedBooleanTrue()).thenReturn(0L)
        whenever(samtykkeRepository.hentAlleLand()).thenReturn(emptyList())

        val valueCaptor = argumentCaptor<String>()
        whenever(kafkaTemplate.send(eq(topic), any(), valueCaptor.capture()))
                .thenReturn(CompletableFuture())

        generateMetricSinkMetrics.generateAndSendMetrics()

        verify(kafkaTemplate, times(1)).send(eq(topic), any(), any())

        val metrikk = objectMapper.readValue<MetricSinkMetrikk>(valueCaptor.firstValue)
        val payload = objectMapper.readValue<EuresCvEksportMetrikkPayload>(metrikk.payload)

        assertEquals(0L, payload.antallSamtykker)
        assertTrue(payload.antallPerLand.isEmpty())
    }

    @Test
    fun `should not propagate exceptions from repository`() {
        whenever(samtykkeRepository.hentAntallSamtykkerMedBooleanTrue())
                .thenThrow(RuntimeException("DB error"))

        assertDoesNotThrow { generateMetricSinkMetrics.generateAndSendMetrics() }
        verify(kafkaTemplate, never()).send(any(), any(), any())
    }

    @Test
    fun `payload should be valid json string inside metrikk`() {
        whenever(samtykkeRepository.hentAntallSamtykkerMedBooleanTrue()).thenReturn(5L)
        whenever(samtykkeRepository.hentAlleLand()).thenReturn(listOf("""["FI"]"""))

        val valueCaptor = argumentCaptor<String>()
        whenever(kafkaTemplate.send(eq(topic), any(), valueCaptor.capture()))
                .thenReturn(CompletableFuture())

        generateMetricSinkMetrics.generateAndSendMetrics()

        val outerMap = objectMapper.readValue<Map<String, Any>>(valueCaptor.firstValue)
        assertEquals("EURES_CV_EKSPORT", outerMap["type"])

        // payload field should be a string (not nested object) per pam-metric-sink contract
        assertTrue(outerMap["payload"] is String, "payload should be a JSON string, not a nested object")

        // that string should itself be valid JSON
        val payloadMap = objectMapper.readValue<Map<String, Any>>(outerMap["payload"] as String)
        assertEquals(5, payloadMap["antallSamtykker"])
        assertEquals(mapOf("FI" to 1), payloadMap["antallPerLand"])
    }
}
