package no.nav.cv.eures.model.dto

import no.nav.arbeid.cv.avro.Arbeidserfaring
import no.nav.cv.eures.janzz.dto.CachedEscoMapping

data class ArbeidserfaringWithEscoDto(
    val arbeidserfaring: Arbeidserfaring,
    val occupationEscoMapping: CachedEscoMapping
)