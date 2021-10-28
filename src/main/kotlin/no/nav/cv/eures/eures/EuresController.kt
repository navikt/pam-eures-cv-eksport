package no.nav.cv.eures.eures

import no.nav.cv.eures.model.Converters.toUtcZonedDateTime
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("input/api/cv/v1.0")
@Unprotected
class EuresController(
    private val euresService: EuresService
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(EuresController::class.java)
    }

    @GetMapping("ping")
    fun ping() = "Hello from Input API"

    @GetMapping("getAll", produces = ["application/json"])
    fun getAll() =
        euresService.getAllReferences()
            .also { log.info("EURES connecting to getAll(), returning ${it.allReferences.size} references") }

    @GetMapping("getChanges/{modificationTimestamp}", produces = ["application/json"])
    fun getChanges(@PathVariable("modificationTimestamp") modificationTimestamp: Long) =
        euresService.getChangedReferences(modificationTimestamp.toUtcZonedDateTime())
            .also {
                log.info(
                    "EURES connecting to getChanges() with argument ${modificationTimestamp.toUtcZonedDateTime()}," +
                            " returning ${it.createdReferences.size} createdReferences" +
                            " returning ${it.modifiedReferences.size} modifiedReferences" +
                            " returning ${it.closedReferences.size} closedReferences"
                )
            }

    @PostMapping("getDetails", consumes = ["application/json"], produces = ["application/json"])
    fun getDetails(@RequestBody references: List<String>) =
        euresService.getDetails(references)
            .also {
                log.info(
                    "EURES connecting to getDetails() with ${references.size} references as argument," +
                            " returning ${it.details.size} details"
                )
            }

}
