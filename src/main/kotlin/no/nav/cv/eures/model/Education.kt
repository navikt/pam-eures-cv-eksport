package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

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
        val code: String

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

