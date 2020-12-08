package no.nav.cv.eures.eures

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class EuresConfig(
        @Value("\${eures.token}") private val ourToken: ByteArray
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(EuresSecurityHandler(ourToken))
    }
}