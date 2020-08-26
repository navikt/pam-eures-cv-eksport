package no.nav.cv.eures.samtykke

import java.time.ZonedDateTime

data class Samtykke(
        val aktoerId: String,
        val sistEndret: ZonedDateTime,
        val personalia: Boolean,
        val utdanning: Boolean
)