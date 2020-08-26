package no.nav.cv.eures.samtykke

import io.micronaut.http.annotation.*

@Controller("samtykke")
class SamtykkeController(
        val samtykkeRepository: SamtykkeRepository
) {

    @Get("/{aktoerId}", produces = [ "application/json" ])
    fun hentSamtykke(aktoerId: String) : Samtykke
            = samtykkeRepository.hentSamtykke(aktoerId)
            ?: throw Exception("not found")

    @Post("/{aktoerId}", produces = [ "application/json" ])
    fun oppdaterSamtykke(aktoerId: String, @Body samtykke: Samtykke)
            = samtykkeRepository.oppdaterSamtykke(samtykke)

    @Delete("/{aktoerId}", produces = [ "application/json" ])
    fun slettSamtykke(aktoerId: String, @Body samtykke : Samtykke)
            = samtykkeRepository.slettSamtykke(samtykke)
}