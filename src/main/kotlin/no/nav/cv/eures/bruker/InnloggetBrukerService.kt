package no.nav.cv.eures.bruker

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Service

@Service
class InnloggetBrukerService (
    private val contextHolder: TokenValidationContextHolder
) : InnloggetBruker {

    override fun fodselsnummer(): String {
        val fnr = contextHolder.tokenValidationContext.getClaims("selvbetjening").let {
            it.getStringClaim("pid") ?: it.subject
        }
        if (fnr == null || fnr.trim { it <= ' ' }.isEmpty()) {
            throw IllegalStateException("Fant ikke FNR i token")
        }
        return fnr
    }
}