package no.nav.cv.eures.nais

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule

@Controller("/internal")
@Secured(SecurityRule.IS_ANONYMOUS)
class StatusController {

    @Get("isAlive")
    fun isAlive() = "OK"

    @Get("isReady")
    fun isReady() = "OK"
}