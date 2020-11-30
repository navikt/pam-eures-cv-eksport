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
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class EuresSecurityHandler(
        private val ourToken: String
) : HandlerInterceptor {
    private val log: Logger = LoggerFactory.getLogger(EuresSecurityHandler::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        val handlerMethod = handler as HandlerMethod

        if (handlerMethod.bean !is EuresController)
            return true

        if (request.extractToken()?.equals(ourToken) == true)
            return true

        response.status = HttpStatus.UNAUTHORIZED.value()
        return false

    }

    fun HttpServletRequest.extractToken(): String? = getHeader("Authorization")?.split(' ')?.get(1)

}