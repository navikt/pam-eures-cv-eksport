package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import no.nav.arbeid.cv.avro.Utdannelse
import no.nav.cv.eures.konverterer.toFormattedDateTime

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
    val attendancePeriod: AttendancePeriod,
    val educationDegree: EducationDegree?
) {
    constructor(utdannelse: Utdannelse): this(
        organizationName = utdannelse.laerested,
        programName = "${utdannelse.utdanningsretning} - ${utdannelse.beskrivelse}",
        attendancePeriod = AttendancePeriod(
            utdannelse.fraTidspunkt?.toFormattedDateTime(),
            utdannelse.tilTidspunkt?.toFormattedDateTime()
        ),
        educationLevelCode = EducationLevelCode(code = utdannelse.nuskodeGrad.substring(0,1)),
        educationDegree = educationDegree(utdannelse.nuskodeGrad.substring(0,1))
    )
}

// 4.28.12
data class EducationLevelCode(
    @JacksonXmlProperty(isAttribute = true, localName = "listName")
    val listName: String = "EURES_EQF",

    @JacksonXmlProperty(isAttribute = true, localName = "listURI")
    val listURI: String = "http://ec.europa.eu/esco/ConceptScheme/EQF2012/ConceptScheme",

    @JacksonXmlProperty(isAttribute = true, localName = "listVersionID")
    val listVersionID: String = "2008/C11/01",

    //@JacksonXmlProperty(localName = "text")
    @JacksonXmlText
    val code: String
)

data class EducationDegree(
    val degreeName: String,
    val degreeTypeCode: String
)

private fun educationDegree(code: String): EducationDegree? = when (code) {
    "0" -> EducationDegree(degreeName = "EarlyChildhood", degreeTypeCode = "EarlyChildhood")
    "1" -> EducationDegree(degreeName = "Primary", degreeTypeCode = "Primary")
    "2" -> EducationDegree(degreeName = "LowerSecondary", degreeTypeCode = "LowerSecondary")
    "3" -> EducationDegree(degreeName = "UpperSecondary", degreeTypeCode = "UpperSecondary")
    "4" -> EducationDegree(degreeName = "PostSecondaryNonTertiary", degreeTypeCode = "PostSecondaryNonTertiary")
    "5" -> EducationDegree(degreeName = "ShortCycleTertiary", degreeTypeCode = "ShortCycleTertiary")
    "6" -> EducationDegree(degreeName = "Bachelor", degreeTypeCode = "Bachelor")
    "7" -> EducationDegree(degreeName = "Masters", degreeTypeCode = "Masters")
    "8" -> EducationDegree(degreeName = "Doctoral", degreeTypeCode = "Doctoral")
    else -> null
}

