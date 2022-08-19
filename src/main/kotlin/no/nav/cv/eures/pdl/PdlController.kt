package no.nav.cv.eures.pdl

import no.nav.cv.eures.bruker.InnloggetBrukerService
import no.nav.cv.eures.samtykke.Samtykke
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("pdl")
//@ProtectedWithClaims(issuer = "selvbetjening")
@Unprotected
class PdlController (
    private val innloggetbrukerService: InnloggetBrukerService,
    private val pdlPersonGateway: PdlPersonGateway
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(PdlController::class.java)
    }

    @GetMapping(produces = ["application/json"])
    fun getErEUEOSStatsborgerskap(): ResponseEntity<Boolean> {
        val fnr = innloggetbrukerService.fodselsnummer()
        return ResponseEntity.ok(pdlPersonGateway.erEUEOSstatsborger(fnr))
    }
}



