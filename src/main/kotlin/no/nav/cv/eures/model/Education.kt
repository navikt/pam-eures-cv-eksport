package no.nav.cv.eures.model

import no.nav.arbeid.cv.avro.Cv
import no.nav.cv.eures.samtykke.Samtykke
import java.time.LocalDate

class Education(
        private val cv: Cv,
        private val samtykke: Samtykke)
{
    fun getEducationHistory() : EducationHistory {
        val utdanninger = mutableListOf<OrganizationAttendance>()

        // TODO : Antar man alltid maa ha med en utdanning typ grunnskole
        if(!samtykke.utdanning)
            return EducationHistory(listOf())

        return EducationHistory(
                listOf(OrganizationAttendance(
                        organizationName = "Test skole",
                        educationLevel = EducationLevel.Bachelor,
                        attendancePeriod = AttendancePeriod(LocalDate.of(2010, 8, 1).toString(), null)
                )))
    }
}


// 4.13
data class EducationHistory(
        val organizationAttendance: List<OrganizationAttendance>
)

// 4.13.3
data class OrganizationAttendance(
        val organizationName: String,
        val educationLevel: EducationLevel,
        val attendancePeriod: AttendancePeriod
)

// 4.28.12
enum class EducationLevel(code: Int) {
    EarlyChildhood(0),
    Primary(1),
    LowerSecondary(2),
    UpperSecondary(3),
    PostSecondaryNonTertiary(4),
    ShortCycleTertiary(5),
    Bachelor(6),
    Masters(7),
    Doctoral(8)
}

// 4.13.7.2
data class AttendancePeriod(
        val startDate: String,
        val endDate: String?
)