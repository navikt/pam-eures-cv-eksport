//package no.nav.cv.eures.security
//
//import io.micronaut.http.HttpRequest
//import io.micronaut.http.HttpResponse
//import io.micronaut.http.annotation.Filter
//import io.micronaut.http.filter.FilterChain
//import io.micronaut.http.filter.HttpFilter
//import io.micronaut.runtime.http.scope.RequestScope
//import org.reactivestreams.Publisher
//import java.util.stream.Collectors
//
//private const val bearer = "Bearer"
//
//@Filter(Filter.MATCH_ALL_PATTERN)
//class MicronautJwtTokenValidationFilter(
//) : HttpFilter {
//
//    override fun doFilter(request: HttpRequest<*>, chain: FilterChain): Publisher<out HttpResponse<*>> {
//        request.headers.authorization
//                .map { it.split(" ")[1].map { Jwt } }
//    }
//
//    private fun getTokensFromHeader(config: MultiIssuerConfiguration, request: HttpRequest<*>): List<JwtToken?>? {
//        try {
//            LOG.debug("checking authorization header for tokens")
//            val authorization: String = request.getHeader(AUTHORIZATION_HEADER)
//            if (authorization != null) {
//                val headerValues = authorization.split(",").toTypedArray()
//                return extractBearerTokens(headerValues)
//                        .stream()
//                        .map({ JwtToken() })
//                        .filter({ jwtToken -> config.getIssuer(jwtToken.getIssuer()).isPresent() })
//                        .collect(Collectors.toList())
//            }
//            LOG.debug("no tokens found in authorization header")
//        } catch (e: Exception) {
//            LOG.warn("received exception when attempting to extract and parse token from Authorization header", e)
//        }
//        return Collections.emptyList()
//    }
//}
//
//@RequestScope
//class TokenHolder {
//
//    var token: String = ""
//
//}