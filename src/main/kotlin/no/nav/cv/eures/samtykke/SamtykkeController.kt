package no.nav.cv.eures.samtykke

import io.micronaut.http.annotation.*
import no.nav.cv.eures.cv.CvConsumer
import org.slf4j.LoggerFactory

@Controller("samtykke")
class SamtykkeController(
        private val samtykkeRepository: SamtykkeRepository
) {
    companion object {
        val log = LoggerFactory.getLogger(SamtykkeController::class.java)
    }

    @Get("/{aktoerId}", produces = [ "application/json" ])
    fun hentSamtykke(aktoerId: String)
            = samtykkeRepository.hentSamtykke(aktoerId)
            ?: throw Exception("not found")


    @Post("/{aktoerId}")
    fun oppdaterSamtykke(@Body samtykke: Samtykke) : String {
        samtykkeRepository.oppdaterSamtykke(samtykke)
        return "OK"
    }

    @Delete("/{aktoerId}", produces = [ "application/json" ])
    fun slettSamtykke(aktoerId: String)
            = samtykkeRepository.slettSamtykke(aktoerId)
}