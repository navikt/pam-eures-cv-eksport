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
    fun hentSamtykke(aktoerId: String) : Samtykke {
        log.debug("Henter $aktoerId")
        val samtykke = samtykkeRepository.hentSamtykke(aktoerId)


        log.debug("Fra repo $samtykke")

        return samtykke ?: throw Exception("not found")
    }

    @Post("/{aktoerId}")
    fun oppdaterSamtykke(@Body samtykke: Samtykke) : String {
        samtykkeRepository.oppdaterSamtykke(samtykke)
        return "OK"
    }

    @Delete("/{aktoerId}", produces = [ "application/json" ])
    fun slettSamtykke(aktoerId: String)
            = samtykkeRepository.slettSamtykke(aktoerId)
}