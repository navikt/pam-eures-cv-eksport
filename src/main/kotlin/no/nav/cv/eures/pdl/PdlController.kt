package no.nav.cv.eures.pdl

import no.nav.cv.eures.bruker.InnloggetBruker
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        val erEUEOSstatsborger = EUEOSstatsborgerskap(erEUEOSborger = pdlPersonGateway.erEUEOSstatsborger(fnr) ?: false)
        log.info("Person er statsborger i EU/EØS: $erEUEOSstatsborger")
        return ResponseEntity.ok(erEUEOSstatsborger)
    }
}

data class EUEOSstatsborgerskap(
    val erEUEOSborger: Boolean
)


