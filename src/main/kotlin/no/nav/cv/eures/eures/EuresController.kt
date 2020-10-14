package no.nav.cv.eures.eures

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import no.nav.cv.eures.model.Converters.toUtcZonedDateTime

@Controller("input/api/cv/v1.0")
class EuresController(
        private val euresService: EuresService
) {

    @Get("ping")
    fun ping() = "Hello from Input API"

    @Get("getAll", produces = ["application/json"])
    fun getAll() =
            euresService.getAllReferences()

    @Get("getChanges/{modificationTimestamp}", produces = ["application/json"])
    fun getChanges(modificationTimestamp: Long) =
            euresService.getChangedReferences(modificationTimestamp.toUtcZonedDateTime())

    @Post("getDetails", produces = ["application/json"])
    fun getDetails(@Body references: List<String>) =
            euresService.getDetails(references)

}
