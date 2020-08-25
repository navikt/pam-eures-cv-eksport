package no.nav.cv.eures.samtykke

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.http.exceptions.HttpException
import java.lang.Exception
import java.net.http.HttpResponse

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
    fun slettSamtykke(aktoerId: String)
            = samtykkeRepository.slettSamtykke(aktoerId)
}