package no.nav.cv.eures.model

// 4.13.7.2
data class AttendancePeriod(
        val startDate: FormattedDateTime?,
        val endDate: FormattedDateTime?
)

data class FormattedDateTime(val formattedDateTime: String)