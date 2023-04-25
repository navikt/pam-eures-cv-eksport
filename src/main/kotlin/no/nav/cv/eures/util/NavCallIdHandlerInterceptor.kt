package no.nav.cv.eures.util

import org.apache.commons.lang3.StringUtils
import org.slf4j.MDC
import org.springframework.lang.Nullable
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.Exception
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class NavCallIdHandlerInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        var callId = request.getHeader(NAV_CALL_ID_HEADER_NAME)
        if (StringUtils.isBlank(callId)) {
            callId = UUID.randomUUID().toString()
        }
        MDC.put(NAV_CALL_ID_MDC_KEY, callId)
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        @Nullable ex: Exception?
    ) {
        MDC.remove(NAV_CALL_ID_MDC_KEY)
    }

    companion object {
        const val NAV_CALL_ID_HEADER_NAME = "Nav-CallId"
        const val NAV_CALL_ID_MDC_KEY = "Nav-CallId"
    }
}
