package no.nav.cv.eures.eures

import no.nav.cv.eures.model.Converters.toUtcZonedDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import java.time.ZoneId
import java.time.ZonedDateTime


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EuresControllerTest {

    companion object {
        const val EURES_REQUIRED_PING_CONSTANT = "Hello from Input API"
    }

    @LocalServerPort
    private var randomServerPort = 0
    private var baseUrl = ""
    private val client = TestRestTemplate()

    @BeforeEach
    fun setup() {
        baseUrl = "http://localhost:${randomServerPort}/pam-eures-cv-eksport/"
    }

    @Test
    fun `ping controller skal inkludere constant string i returnert verdi` () {
        val body = client.getForEntity("${baseUrl}input/api/cv/v1.0/ping", String::class.java).body
        assertEquals(true, body?.contains(EURES_REQUIRED_PING_CONSTANT))
    }

    @Test
    fun `konvertering av long til UTC ZonedDateTime skal vaere korrekt` () {
        // 01/17/1988 02:45:00 UTC+1
        val long = 569382300000
        assertEquals(
                long.toUtcZonedDateTime(),
                ZonedDateTime.of(1988, 1, 17, 2, 45, 0, 0, ZoneId.of("Europe/Oslo"))
                        .withZoneSameInstant(ZoneId.of("UTC"))
        )
    }

}
