package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.arbeid.cv.avro.Kurs
import no.nav.arbeid.cv.avro.Tidsenhet
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternCourse(
    val title: String?,
    val issuer: String?,
    val duration: Int?,
    val durationUnit: String?,
    val date: ZonedDateTime?
)