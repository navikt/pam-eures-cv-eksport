package no.nav.cv.eures.pdl

import no.nav.cv.eures.bruker.InnloggetBruker
import no.nav.cv.eures.bruker.InnloggetBrukerService
import no.nav.cv.eures.samtykke.Samtykke
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("pdl")
@ProtectedWithClaims(issuer = "selvbetjening")
class PdlController (
    private val innloggetBrukerService: InnloggetBruker,
    private val pdlPersonGateway: PdlPersonGateway
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(PdlController::class.java)
    }

    @GetMapping(produces = ["application/json"])
    fun getErEUEOSStatsborgerskap(): ResponseEntity<EUEOSstatsborgerskap> {
        val fnr = innloggetBrukerService.fodselsnummer()
        return ResponseEntity.ok(EUEOSstatsborgerskap(erEUEOSborger = pdlPersonGateway.erEUEOSstatsborger(fnr) ?: false))
    }
}

data class EUEOSstatsborgerskap(
    val erEUEOSborger: Boolean
)


