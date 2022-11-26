package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.arbeid.cv.avro.Utdannelse
import no.nav.arbeid.cv.avro.UtdannelseYrkestatus
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternEducation(
    val institution: String?,
    val field: String?,
    val nuskode: String?,
    val hasAuthorization: Boolean?,
    val vocationalCollege: String?,
    val startDate: ZonedDateTime?,
    val endDate: ZonedDateTime?,
    val description: String?
)