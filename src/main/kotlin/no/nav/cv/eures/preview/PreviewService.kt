package no.nav.cv.eures.preview

import no.nav.cv.eures.janzz.JanzzService
import no.nav.cv.eures.konverterer.CvConverterService2
import no.nav.cv.eures.model.Candidate
import org.springframework.stereotype.Service

@Service
class PreviewService(
    private val cvConverterService2: CvConverterService2,
    private val janzzService: JanzzService
) {

    fun getPreviewDto(fnr: String): PreviewDto {
        val (_, _, candidate) = cvConverterService2.convertToXml(fnr)
            ?: return PreviewDto()

        return PreviewDto(
            candidate = candidate,
            escoMap = generateEscoMap(candidate)
        )

    }

    private fun generateEscoMap(candiate: Candidate): Map<String, String?> {
        val escoMap = mutableMapOf<String, String?>()
        candiate.candidateProfile.employmentHistory?.employerHistory?.forEach { employerHistory ->
            employerHistory.positionHistory.forEach { positionHistory ->
                positionHistory.jobCategoryCode?.let { jobCategoryCode ->
                    escoMap[jobCategoryCode.code] = janzzService.getTermForEsco(jobCategoryCode.code)
                }
            }
        }
        candiate.candidateProfile.personQualifications?.personCompetency?.forEach { personCompetency ->
                escoMap[personCompetency.competencyID] = janzzService.getTermForEsco(personCompetency.competencyID)
        }
        return escoMap
    }
}