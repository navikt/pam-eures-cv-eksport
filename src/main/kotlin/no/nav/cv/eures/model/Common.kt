package no.nav.cv.eures.model

// 4.13.7.2
data class AttendancePeriod(
        val startDate: PeriodStartDate,
        val endDate: FormattedDateTime?
)

data class FormattedDateTime(val formattedDateTime: String) : PeriodStartDate()
data class DateText(val dateText: String) : PeriodStartDate()

open class PeriodStartDate