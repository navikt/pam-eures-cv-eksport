package no.nav.cv.eures.samtykke

import io.swagger.v3.oas.annotations.Operation
import no.nav.cv.eures.bruker.InnloggetBruker
import no.nav.cv.eures.pdl.PdlPersonGateway
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("samtykke")
@ProtectedWithClaims(issuer = "selvbetjening")
class SamtykkeController(
    private val samtykkeService: SamtykkeService,
    private val innloggetbrukerService: InnloggetBruker,
    private val pdlPersonGateway: PdlPersonGateway
) {
        companion object {
        val log: Logger = LoggerFactory.getLogger(SamtykkeController::class.java)
    }

    @Operation(summary = "Henter et samtykke", description = "Henter et samtykke basert på fnr i claim")
    @GetMapping(produces = ["application/json"])
    fun hentSamtykke(): ResponseEntity<Samtykke> {
        val fnr = extractFnr()
        return samtykkeService.hentSamtykke(fnr)
            ?.let { ResponseEntity.ok(it) }
            ?: run {
                ResponseEntity.notFound().build()
            }
    }


    @Operation(summary = "Oppdaterer et samtykke", description = "Oppdaterer et samtyke basert på fnr i claim")
    @PostMapping(produces = ["application/json"])
    fun oppdaterSamtykke(
        @RequestBody samtykke: Samtykke
    ): ResponseEntity<Samtykke> {
        when (pdlPersonGateway?.erEUEOSstatsborger(extractFnr()) ?: false) {
            false -> {
                return ResponseEntity.status(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).body(null)
            }
        }
        samtykkeService.oppdaterSamtykke(extractFnr(), samtykke)
        return ResponseEntity.ok(samtykke)
    }

    @Operation(summary = "Sletter et samtykke", description = "Sletter et samtykke basert på fnr i claim")
    @DeleteMapping(produces = ["application/text"])
    fun slettSamtykke(): ResponseEntity<String> {
        samtykkeService.slettSamtykke(extractFnr())
        return ResponseEntity.ok("OK")
    }

    private fun extractFnr(): String = innloggetbrukerService.fodselsnummer()

}

