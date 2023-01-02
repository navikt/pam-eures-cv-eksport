package no.nav.cv.eures.konverterer

import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.eures.model.CandidateProfile
import no.nav.cv.eures.samtykke.Samtykke

class CandidateProfileConverter(
    private val dto: CvEndretInternDto,
    private val samtykke: Samtykke
) {
    companion object {
        val xml10Pattern = Regex("[^"+ "\u0009\r\n"+ "\u0020-\uD7FF"+ "\uE000-\uFFFD"+ "\ud800\udc00-\udbff\udfff"+ "]")
    }

    private val ikkeSamtykket = ""

    fun toXmlRepresentation()
            = CandidateProfile(
            executiveSummary = if(samtykke.sammendrag) dto.cv?.summary.orEmpty() else ikkeSamtykket,
            employmentHistory = if(samtykke.arbeidserfaring) EmploymentHistoryConverter(dto).toXmlRepresentation() else null,
            educationHistory = if(samtykke.utdanning) EducationHistoryConverter(dto).toXmlRepresentation() else null,
            licenses = if(samtykke.foererkort) LicensesConverter(dto).toXmlRepresentation() else null,
            certifications = CertificationConverter(dto, samtykke).toXmlRepresentation(),
            personQualifications = PersonQualificationsConverter(dto, samtykke).toXmlRepresentation()
    )
}