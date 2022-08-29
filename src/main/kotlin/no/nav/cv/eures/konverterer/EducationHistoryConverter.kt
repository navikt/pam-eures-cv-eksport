package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Utdannelse
import no.nav.cv.eures.model.*
import no.nav.cv.eures.samtykke.Samtykke



class EducationHistoryConverter(
        private val cv: Cv,
        private val samtykke: Samtykke
) {
    private val ikkeSamtykket = null

    fun toXmlRepresentation()
            = when(samtykke.utdanning) {
        true -> EducationHistory(cv.utdannelse.toEducationList())
        false -> ikkeSamtykket
    }

    // TODO støtte null i fra-tidpsunkt
    private fun List<Utdannelse>.toEducationList()
            = map { EducationOrganizationAttendance(
            organizationName = it.laerested,
            programName = "${it.utdanningsretning} - ${it.beskrivelse}",
            attendancePeriod = AttendancePeriod(
                    it.fraTidspunkt?.toFormattedDateTime() ?: DateText("Unknown"),
                    it.tilTidspunkt?.toFormattedDateTime()),
            educationLevelCode = EducationLevelCode(code = it.nuskodeGrad.toEducationLevelCode()),
            educationDegree = educationDegree(it.nuskodeGrad.toEducationLevelCode())
    ) }

    private fun String.toEducationLevelCode() = substring(0, 1)

    private fun educationDegree(code: String) : EducationDegree?
        = when(code) {
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

}
