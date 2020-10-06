package no.nav.cv.eures.samtykke

import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory

@Controller("samtykke")
class SamtykkeController(
        private val samtykkeService: SamtykkeService
) {
    companion object {
        val log = LoggerFactory.getLogger(SamtykkeController::class.java)
    }

    @Get("/{aktoerId}", produces = [ "application/json" ])
    fun hentSamtykke(aktoerId: String)
            = samtykkeService.hentSamtykke(aktoerId)
            ?: throw Exception("not found")


    @Post("/{aktoerId}")
    fun oppdaterSamtykke(@Body samtykke: Samtykke) : String {
        samtykkeService.oppdaterSamtykke(samtykke)
        return "OK"
    }

    @Delete("/{aktoerId}", produces = [ "application/json" ])
    fun slettSamtykke(aktoerId: String)
            = samtykkeService.slettSamtykke(aktoerId)
}
