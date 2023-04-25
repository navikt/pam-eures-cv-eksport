package no.nav.cv.eures.bruker

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.springframework.stereotype.Service

@Service
class InnloggetBrukerService (
    private val contextHolder: TokenValidationContextHolder
) : InnloggetBruker {

    override fun fodselsnummer(): String {
        val context = contextHolder.tokenValidationContext
        val issuers = context.issuers
        var claims : JwtTokenClaims? = null

        if(issuers.isNotEmpty()) {
            claims = context.getClaims(issuers[0])
        }

        if(claims == null) {
            throw IllegalStateException("Fant ikke FNR i token")
        }

        return claims.getStringClaim("pid") ?: claims.subject
    }
}