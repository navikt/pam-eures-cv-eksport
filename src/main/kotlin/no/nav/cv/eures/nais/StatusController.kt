package no.nav.cv.eures.nais

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/internal/status")
class StatusController {

    @Get("alive")
    fun isAlive() = "OK"

    @Get("ready")
    fun isReady() = "OK"
}