package no.nav.cv.eures.preview

import no.nav.cv.eures.bruker.InnloggetBruker
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("preview")
@RequiredIssuers(
    ProtectedWithClaims(issuer = "tokenx")
)
class PreviewController(
    private val previewService: PreviewService,
    private val innloggetbrukerService: InnloggetBruker
) {

    @GetMapping(produces = ["application/json"])
    fun getPreview() = previewService.getPreviewDto(innloggetbrukerService.fodselsnummer())

}