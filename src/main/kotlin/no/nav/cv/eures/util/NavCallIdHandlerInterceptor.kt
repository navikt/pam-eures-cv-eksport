package no.nav.cv.eures.util

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.Exception
import java.util.*



class NavCallIdHandlerInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val callId = request.getHeader(NAV_CALL_ID_HEADER_NAME)
            .takeUnless { it.isNullOrBlank() }
            ?: UUID.randomUUID().toString()
        MDC.put(NAV_CALL_ID_MDC_KEY, callId)
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        MDC.remove(NAV_CALL_ID_MDC_KEY)
    }

    companion object {
        const val NAV_CALL_ID_HEADER_NAME = "Nav-CallId"
        const val NAV_CALL_ID_MDC_KEY = "Nav-CallId"
    }
}
