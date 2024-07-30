package no.nav.cv.eures.bruker

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class InnloggetBrukerService (
    private val contextHolder: TokenValidationContextHolder
) : InnloggetBruker {
    companion object {
        val logger = LoggerFactory.getLogger(InnloggetBrukerService::class.java)
    }

    override fun fodselsnummer(): String {
        val context = contextHolder.tokenValidationContext
        val issuers = context.issuers
        var claims : JwtTokenClaims? = null

        if(issuers.isNotEmpty()) {
            try {
                claims = context.getClaims(issuers[0])
            } catch (e: Exception) {
                logger.warn("Kunne ikke hente claims fra token i InnloggetBrukerService", e)
                throw e
            }
        }

        if(claims == null) {
            throw IllegalStateException("Fant ikke FNR i token")
        }

        return claims.getStringClaim("pid") ?: claims.subject
    }
}