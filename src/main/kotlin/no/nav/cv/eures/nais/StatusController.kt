package no.nav.cv.eures.nais

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/internal")
class StatusController {

    @Get("isAlive")
    fun isAlive() = "OK"

    @Get("isReady")
    fun isReady() = "OK"
}