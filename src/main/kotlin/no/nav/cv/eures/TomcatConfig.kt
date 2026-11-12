package no.nav.cv.eures

import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private const val UNLIMITED_LOCALE_COUNT = -1

// Tomcat 11.0.27 fails requests with more than 10 Accept-Language locales. Replace with
// server.tomcat.max-locale-count in Spring Boot 4.1.3, and remove when Spring Boot ships Tomcat >= 11.0.29.
@Configuration
class TomcatConfig {

    @Bean
    fun unlimitedLocaleCount() = WebServerFactoryCustomizer<TomcatServletWebServerFactory> { factory ->
        factory.addConnectorCustomizers({ connector -> connector.maxLocaleCount = UNLIMITED_LOCALE_COUNT })
    }
}
