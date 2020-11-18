package no.nav.cv.eures.konverterer

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class KonverterConfig {

    @Bean
    fun restTemplate(builder: RestTemplateBuilder) = builder.build()
}