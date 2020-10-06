package no.nav.cv.eures.eures

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import java.sql.Timestamp

@Controller("input/api/cv/v1.0")
class EuresController(
        val euresService: EuresService
) {

    @Get("ping")
    fun ping() = "Hello from Input API"

    @Get("getAll", produces = ["application/json"])
    fun getAll() = euresService.getAllReferences()

    @Get("getChanges/{modificationTimestamp}", produces = ["application/json"])
    fun getChanges(modificationTimestamp: Timestamp) = euresService.getChangedReferences(modificationTimestamp)

    @Post("getDetails", produces = ["application/json"])
    fun getDetails(@Body ids: List<String>) = CvDetails(mapOf(ids.first() to CvDetails.CandidateDetail()))

}
