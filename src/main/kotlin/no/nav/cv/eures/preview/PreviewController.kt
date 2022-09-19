package no.nav.cv.eures.preview

import no.nav.cv.eures.samtykke.InnloggetBrukerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("preview")
@ProtectedWithClaims(issuer = "selvbetjening")
class PreviewController(
    private val previewService: PreviewService,
    private val innloggetbrukerService: InnloggetBrukerService
) {

    @GetMapping(produces = ["application/json"])
    fun getPreview() = previewService.getPreviewDto(innloggetbrukerService.fodselsnummer())

}