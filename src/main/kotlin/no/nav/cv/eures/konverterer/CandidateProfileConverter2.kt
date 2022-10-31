package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Jobbprofil
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.eures.model.CandidateProfile
import no.nav.cv.eures.samtykke.Samtykke

class CandidateProfileConverter2(
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
            employmentHistory = if(samtykke.arbeidserfaring) EmploymentHistoryConverter2(dto).toXmlRepresentation() else null,
            educationHistory = if(samtykke.utdanning) EducationHistoryConverter2(dto).toXmlRepresentation() else null,
            licenses = if(samtykke.foererkort) LicensesConverter2(dto).toXmlRepresentation() else null,
            certifications = CertificationConverter2(dto, samtykke).toXmlRepresentation(),
            personQualifications = PersonQualificationsConverter2(dto, samtykke).toXmlRepresentation()
    )
}