package no.nav.cv.eures.eures

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerAdapter
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class EuresSecurityHandler(
        private val ourToken: ByteArray
) : HandlerInterceptor {
    private val log: Logger = LoggerFactory.getLogger(EuresSecurityHandler::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod)
            return true

        val handlerMethod = handler as HandlerMethod

        if (handlerMethod.bean !is EuresController)
            return true

        if (request.hasToken() && request.token contentEquals ourToken )
            return true

        log.debug("Invalid or missing token on call to EuresController. Returning 401 UNAUTHORIZED")
        response.status = HttpStatus.UNAUTHORIZED.value()
        return false

    }

    val HttpServletRequest.token
        get() = getHeader("Authorization")
                ?.split(' ')
                ?.get(1)
                ?.let { Base64.getDecoder().decode(it) }

    fun HttpServletRequest.hasToken() = token != null


}