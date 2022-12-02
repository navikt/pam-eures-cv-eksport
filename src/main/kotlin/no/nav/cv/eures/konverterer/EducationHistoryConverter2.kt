package no.nav.cv.eures.konverterer

import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.cv.CvEndretInternEducation
import no.nav.cv.eures.model.*



class EducationHistoryConverter2(
        private val dto: CvEndretInternDto
) {

    fun toXmlRepresentation() : EducationHistory {
        return EducationHistory(dto.cv?.education?.toEducationList().orEmpty())
    }

    private fun List<CvEndretInternEducation>.toEducationList()
            = map { EducationOrganizationAttendance(
            organizationName = it.institution ?: "",
            programName = "${it.field} - ${it.description}",
            attendancePeriod = AttendancePeriod(
                    it.startDate?.toFormattedDateTime() ?: DateText("Unknown"),
                    it.endDate?.toFormattedDateTime()),
            //educationLevelCode = EducationLevelCode(code = it.nuskodeGrad.toEducationLevelCode()),
            educationLevelCode = EducationLevelCode(code = it.nuskode?.toEducationLevelCode().orEmpty()),
            educationDegree = educationDegree(it.nuskode?.toEducationLevelCode().orEmpty())
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
