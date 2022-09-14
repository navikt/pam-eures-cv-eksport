package no.nav.cv.eures.helsesjekk

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/")
@Unprotected
class PingController() {
    companion object {
        val log: Logger = LoggerFactory.getLogger(PingController::class.java)
    }

    @GetMapping("ping")
    fun ping(): Ping {
        log.debug("Ping")
        return Ping(status = "OK")
    }

    data class Ping(val status: String)
}
