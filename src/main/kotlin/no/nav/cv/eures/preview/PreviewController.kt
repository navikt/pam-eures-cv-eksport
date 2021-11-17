package no.nav.cv.eures.preview

import no.nav.arbeid.cv.avro.*
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.cv.RawCV
import no.nav.cv.eures.konverterer.CvConverterService
import no.nav.cv.eures.model.Candidate
import no.nav.cv.eures.samtykke.InnloggetBrukerService
import no.nav.cv.eures.samtykke.Samtykke
import no.nav.cv.eures.samtykke.SamtykkeRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

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