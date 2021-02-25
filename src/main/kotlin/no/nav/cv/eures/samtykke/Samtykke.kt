package no.nav.cv.eures.samtykke

import java.time.ZonedDateTime

data class Samtykke(
        val sistEndret: ZonedDateTime = ZonedDateTime.now(),
        val personalia: Boolean = false,
        val utdanning: Boolean = false,
        val fagbrev: Boolean = false, // TODO Er denne i  bruk som fagdokumentasjon?
        val arbeidserfaring: Boolean = false,
        val annenErfaring: Boolean = false,
        val foererkort: Boolean = false,
        val lovregulerteYrker: Boolean = false, // TODO Denne kan fjernes .Gunn, er dette det samme som offentlige godkjenninger
        val offentligeGodkjenninger: Boolean = false,
        val andreGodkjenninger: Boolean = false,
        val kurs: Boolean = false,
        val spraak: Boolean = false,
        val sammendrag: Boolean = false,
        val kompetanser: Boolean = false,

        val land: List<String> = listOf()
)

