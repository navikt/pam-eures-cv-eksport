package no.nav.cv.eures

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework", "no.nav.cv.eures.eures"])
@EnableScheduling
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}