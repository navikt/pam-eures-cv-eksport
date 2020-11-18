package no.nav.cv.eures.nais

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller("/internal")
class StatusController {

    @GetMapping("isAlive")
    fun isAlive() = "OK"

    @GetMapping("isReady")
    fun isReady() = "OK"
}