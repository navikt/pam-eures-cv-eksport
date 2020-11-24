package no.nav.cv.eures.nais

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal")
class StatusController {

    @GetMapping("isAlive")
    fun isAlive() = "OK"

    @GetMapping("isReady")
    fun isReady() = "OK"
}