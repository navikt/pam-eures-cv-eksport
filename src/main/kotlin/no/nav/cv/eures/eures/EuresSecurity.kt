package no.nav.cv.eures.eures

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.util.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class EuresSecurityHandler(
        private val ourToken: ByteArray
) : HandlerInterceptor {
    private val log: Logger = LoggerFactory.getLogger(EuresSecurityHandler::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod)
            return true

        if (handler.bean !is EuresController)
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