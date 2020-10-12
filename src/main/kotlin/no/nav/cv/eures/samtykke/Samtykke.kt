package no.nav.cv.eures.samtykke

import java.time.ZonedDateTime

data class Samtykke(
        val foedselsnummer: String,
        val sistEndret: ZonedDateTime,
        val personalia: Boolean = false,
        val utdanning: Boolean = false,
        val fagbrev: Boolean = false,
        val arbeidserfaring: Boolean = false,
        val annenErfaring: Boolean = false,
        val foererkort: Boolean = false,
        val lovregulerteYrker: Boolean = false,
        val andreGodkjenninger: Boolean = false,
        val kurs: Boolean = false,
        val spraak: Boolean = false,
        val sammendrag: Boolean = false
)