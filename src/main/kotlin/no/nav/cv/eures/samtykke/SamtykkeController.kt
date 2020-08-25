package no.nav.cv.eures.samtykke

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("samtykke")
class SamtykkeController(
        val samtykkeRepository: SamtykkeRepository
) {

    @Get("/{aktoerId}", produces = [ "application/json" ])
    fun hentSamtykke(aktoerId: String) : Samtykke {
        return samtykkeRepository.hentSamtykke(aktoerId)
    }




}