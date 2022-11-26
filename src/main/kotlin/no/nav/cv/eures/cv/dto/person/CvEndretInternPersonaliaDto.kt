package no.nav.cv.dto.person

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternPersonaliaDto(
    val fornavn: String?,
    val etternavn: String?,
    val foedselsdato: LocalDate?,
    val gateadresse: String?,
    val postnummer: String?,
    val kommunenr: String?,
    val poststed: String?,
    val epost: String?,
    val telefon: String?
)
