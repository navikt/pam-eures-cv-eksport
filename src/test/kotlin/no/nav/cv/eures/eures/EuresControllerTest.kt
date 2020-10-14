package no.nav.cv.eures.eures

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import no.nav.cv.eures.model.Converters.toUtcZonedDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest
class EuresControllerTest {

    companion object {
        const val EURES_REQUIRED_PING_CONSTANT = "Hello from Input API"
    }

    @Inject
    @field:Client("/pam-eures-cv-eksport/") lateinit var client: RxHttpClient

    @Test
    fun `ping controller skal inkludere constant string i returnert verdi` () {
        val request = HttpRequest.GET<String>("input/api/cv/v1.0/ping")
        val body = client.toBlocking().retrieve(request)
        assertEquals(true, body.contains(EURES_REQUIRED_PING_CONSTANT))
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
