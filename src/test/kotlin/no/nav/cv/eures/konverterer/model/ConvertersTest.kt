package no.nav.cv.eures.konverterer.model

import no.nav.cv.eures.model.Converters.toUtcZonedDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class ConvertersTest {

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