package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import no.nav.arbeid.cv.avro.Cv
import no.nav.cv.eures.samtykke.Samtykke

class Education(
        private val cv: Cv,
        private val samtykke: Samtykke)
{
    fun getEducationHistory() : EducationHistory {
        val utdanninger = mutableListOf<EducationOrganizationAttendance>()

        // TODO : Antar man alltid maa ha med en utdanning typ grunnskole
        if(!samtykke.utdanning)
            return EducationHistory(listOf())

        for(utd in cv.utdannelse) {
            utdanninger.add(EducationOrganizationAttendance(
                    organizationName = utd.laerested,
                    educationLevelCode = parseEducationLevel(utd.nuskodeGrad),
                    attendancePeriod = AttendancePeriod(
                            FormattedDateTime(utd.fraTidspunkt.toString()),
                            null),
                    programName = utd.utdanningsretning
            ))
        }

        return EducationHistory(utdanninger)
    }

    private fun parseEducationLevel(nuskodeGrad: String) : EducationLevelCode {
        // TODO Finn ut av kode mapping
        return EducationLevelCode(code = EducationLevelCodeEnum.Bachelor.ordinal)
    }
}


// 4.13
data class EducationHistory(
        @JacksonXmlElementWrapper(useWrapping = false)
        val educationOrganizationAttendance: List<EducationOrganizationAttendance>
)

// 4.13.3
data class EducationOrganizationAttendance(
        val organizationName: String,
        val programName: String,
        val educationLevelCode: EducationLevelCode,
        val attendancePeriod: AttendancePeriod
)

// 4.28.12
data class EducationLevelCode(
        @JacksonXmlProperty(isAttribute = true, localName = "listName")
        val listName: String = "EURES_EQF",

        @JacksonXmlProperty(isAttribute = true, localName = "listURI")
        val listURI: String = "http://ec.europa.eu/esco/ConceptScheme/EQF2012/ConceptScheme",

        @JacksonXmlProperty(isAttribute = true, localName = "listVersionID")
        val listVersionID: String = "2008/C11/01",

        @JacksonXmlText
        val code: Int

)
enum class EducationLevelCodeEnum(code: Int) {
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
        val startDate: FormattedDateTime,
        val endDate: FormattedDateTime?
)

data class FormattedDateTime(val formattedDateTime: String)