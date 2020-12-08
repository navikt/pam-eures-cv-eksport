package no.nav.cv.eures.samtykke

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("samtykke")
@ProtectedWithClaims(issuer = "selvbetjening")
class SamtykkeController(
        private val samtykkeService: SamtykkeService,
        private val innloggetbrukerService: InnloggetBrukerService
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(SamtykkeController::class.java)
    }

    @GetMapping(produces = ["application/json"])
    fun hentSamtykke() = samtykkeService.hentSamtykke(extractFnr())
            ?.let{ ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()


    @PostMapping(produces = ["application/json"])
    fun oppdaterSamtykke(
            @RequestBody samtykke: Samtykke
    ): ResponseEntity<Samtykke> {
        samtykkeService.oppdaterSamtykke(extractFnr(), samtykke)
        return ResponseEntity.ok(samtykke)
    }

    @DeleteMapping(produces = ["application/json"])
    fun slettSamtykke(): ResponseEntity<String> {
        samtykkeService.slettSamtykke(extractFnr())
        return ResponseEntity.ok("OK")
    }

    private fun extractFnr(): String = innloggetbrukerService.fodselsnummer()

}


@Component
class InnloggetBrukerService (
        private val contextHolder: TokenValidationContextHolder
) {

    fun fodselsnummer(): String {
        val fnr = contextHolder.tokenValidationContext.getClaims("selvbetjening").subject
        if (fnr == null || fnr.trim { it <= ' ' }.isEmpty()) {
            throw IllegalStateException("Fant ikke FNR i token")
        }
        return fnr
    }
}

