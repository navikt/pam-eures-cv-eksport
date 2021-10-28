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

    @GetMapping("getChanges/{modificationTimestamp}", produces = ["application/json"])
    fun getChanges(@PathVariable("modificationTimestamp") modificationTimestamp: Long) =
        euresService.getChangedReferences(modificationTimestamp.toUtcZonedDateTime())


    @PostMapping("getDetails", consumes = ["application/json"], produces = ["application/json"])
    fun getDetails(@RequestBody references: List<String>) =
        euresService.getDetails(references)


}
