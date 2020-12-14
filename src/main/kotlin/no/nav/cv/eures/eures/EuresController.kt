package no.nav.cv.eures.eures

import no.nav.cv.eures.model.Converters.toUtcZonedDateTime
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("input/api/cv/v1.0")
@Unprotected
class EuresController(
        private val euresService: EuresService
) {

    @GetMapping("ping")
    fun ping() = "Hello from Input API"

    @GetMapping("getAll", produces = ["application/json"])
    fun getAll() =
            euresService.getAllReferences()

    @GetMapping("getChanges/{modificationTimestamp}", produces = ["application/json"])
    fun getChanges(modificationTimestamp: Long) =
            euresService.getChangedReferences(modificationTimestamp.toUtcZonedDateTime())

    @PostMapping("getDetails", consumes = ["application/json"], produces = ["application/json"])
    fun getDetails(@RequestBody references: List<String>) =
            euresService.getDetails(references)

}
