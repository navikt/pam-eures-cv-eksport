package no.nav.cv.eures.samtykke

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@RestController("samtykke")
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

