package no.nav.cv.eures.samtykke

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller("samtykke")
class SamtykkeController(
        private val samtykkeService: SamtykkeService
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(SamtykkeController::class.java)
    }

    // TODO - Actual verification of token, this is just a temporary unwrapping.
    @GetMapping(produces = ["application/json"])
    fun hentSamtykke() = samtykkeService.hentSamtykke(extractFnr())
            ?: ResponseEntity.notFound()


    @PostMapping(produces = ["application/json"])
    fun oppdaterSamtykke(
            samtykke: Samtykke
    ): ResponseEntity<Samtykke> {
        samtykkeService.oppdaterSamtykke(extractFnr(), samtykke)
        return ResponseEntity.ok(samtykke)
    }

    @DeleteMapping(produces = ["application/json"])
    fun slettSamtykke(): ResponseEntity<String> {
        samtykkeService.slettSamtykke(extractFnr())
        return ResponseEntity.ok("OK")
    }

    private fun extractFnr(): String = "dummy"

}
