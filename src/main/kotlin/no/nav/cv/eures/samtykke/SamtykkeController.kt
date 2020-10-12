package no.nav.cv.eures.samtykke

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("samtykke")
class SamtykkeController(
        private val samtykkeService: SamtykkeService
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(SamtykkeController::class.java)
    }

    @Get("/{foedselsnummer}", produces = [ "application/json" ])
    fun hentSamtykke(foedselsnummer: String)
            = samtykkeService.hentSamtykke(foedselsnummer)
            ?: HttpResponse.notFound<String>()


    @Post("/{foedselsnummer}")
    fun oppdaterSamtykke(@Body samtykke: Samtykke) : String {
        samtykkeService.oppdaterSamtykke(samtykke)
        return HttpResponse.ok("OK").toString()
    }

    @Delete("/{foedselsnummer}", produces = [ "application/json" ])
    fun slettSamtykke(foedselsnummer: String)
            = samtykkeService.slettSamtykke(foedselsnummer)

}
