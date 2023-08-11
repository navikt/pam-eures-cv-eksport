package no.nav.cv.eures.pdl

import no.nav.cv.eures.bruker.InnloggetBruker
import no.nav.cv.eures.samtykke.SamtykkeService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("eures/tilgang")
@RequiredIssuers(
    ProtectedWithClaims(issuer = "tokenx")
)
class EuresTilgangController (
    private val innloggetBrukerService: InnloggetBruker,
    private val pdlPersonGateway: PdlPersonGateway,
    private val samtykkeService: SamtykkeService
    ) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(EuresTilgangController::class.java)
    }

    @GetMapping(produces = ["application/json"])
    fun getHarTilgangTilEures(): ResponseEntity<Boolean> {
        val fnr = innloggetBrukerService.fodselsnummer()
        val erEUEOSStatsborger = pdlPersonGateway.erEUEOSstatsborger(fnr) ?: false
        val harSamtykkeFraFor = samtykkeService.hentSamtykke(fnr) != null
        return ResponseEntity.ok(erEUEOSStatsborger || harSamtykkeFraFor)
    }
}

