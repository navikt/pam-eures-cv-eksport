package no.nav.cv.eures.nais

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal")
@Unprotected
class StatusController {

    @GetMapping("isAlive")
    fun isAlive() = "OK"

    @GetMapping("isReady")
    fun isReady() = "OK"
}