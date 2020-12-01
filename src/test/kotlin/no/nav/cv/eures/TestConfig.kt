package no.nav.cv.eures

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import


// Config that enables mocked token handling when running locally - do not delete without testing that you can still start locally afterwards
@Configuration
@Import(TokenGeneratorConfiguration::class)
class TestConfig 