package no.nav.cv.eures.eures

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Controller("input/api/cv/v1.0")
class EuresController(
        val euresService: EuresService
) {

    @Get("ping")
    fun ping() = "Hello from Input API"

    @Get("getAll", produces = ["application/json"])
    fun getAll() =
            euresService.getAllReferences()

    @Get("getChanges/{modificationTimestamp}", produces = ["application/json"])
    fun getChanges(modificationTimestamp: Long) =
            euresService.getChangedReferences(
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(modificationTimestamp), ZoneId.of("UTC")))

    @Post("getDetails", produces = ["application/json"])
    fun getDetails(@Body ids: List<String>) =
            euresService.getDetails(ids)

}
