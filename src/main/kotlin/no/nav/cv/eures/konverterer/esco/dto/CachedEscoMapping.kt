package no.nav.cv.eures.konverterer.esco.dto

import java.time.ZonedDateTime


data class CachedEscoMapping(
        val term: String,
        val conceptId: String,
        val esco: String,
        val updated: ZonedDateTime
)