package no.nav.cv.eures.konverterer

import no.nav.cv.eures.model.FormattedDateTime
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

fun LocalDate.toFormattedDateTime()
        = FormattedDateTime(toString())

fun Long.toFormattedDateTime() : FormattedDateTime {
    val jdf = SimpleDateFormat("yyyy-MM-dd")
    val date = Date(this)

    return FormattedDateTime(jdf.format(date))
}