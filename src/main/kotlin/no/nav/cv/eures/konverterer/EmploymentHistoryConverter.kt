package no.nav.cv.eures.konverterer

import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.cv.CvEndretInternWorkExperience
import no.nav.cv.eures.janzz.JanzzService
import no.nav.cv.eures.model.*

class EmploymentHistoryConverter(
    private val dto: CvEndretInternDto,
    private val janzzService: JanzzService = JanzzService.instance()
) {

    fun toXmlRepresentation() : EmploymentHistory {
       return EmploymentHistory(dto.cv?.workExperience?.toEmploymentList().orEmpty())
    }

    fun List<CvEndretInternWorkExperience>.toEmploymentList() = map {
        EmployerHistory(
                organizationName = it?.employer ?: "",
                employmentPeriod = AttendancePeriod(
                        it.fromDate?.toFormattedDateTime() ?: DateText("Unknown"),
                        it.toDate?.toFormattedDateTime()
                ),
                positionHistory = it.toPositionHistory())
    }

    fun CvEndretInternWorkExperience.toPositionHistory() = listOf(PositionHistory(
            positionTitle = (jobTitle ?: alternativeJobTitle).orEmpty(),
            employmentPeriod = AttendancePeriod(
                    fromDate?.toFormattedDateTime() ?: DateText("Unknown"),
                    toDate?.toFormattedDateTime()
            ),
            jobCategoryCode = jobTitle?.toJobCategoryCode()
    ))

    private fun String.toJobCategoryCode(): JobCategoryCode? = janzzService.getEscoForTerm(this, JanzzService.EscoLookupType.OCCUPATION)
            .firstOrNull()
            ?.let {
                JobCategoryCode(
                        name = it.term,
                        code = it.esco
                )
            }
}
