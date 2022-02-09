package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Jobbprofil
import no.nav.cv.eures.model.CandidateProfile
import no.nav.cv.eures.samtykke.Samtykke

class CandidateProfileConverter(
        private val cv: Cv,
        private val profile: Jobbprofil?,
        private val samtykke: Samtykke
) {
    companion object {
        val xml10Pattern = Regex("[^"+ "\u0009\r\n"+ "\u0020-\uD7FF"+ "\uE000-\uFFFD"+ "\ud800\udc00-\udbff\udfff"+ "]")
    }

    private val ikkeSamtykket = ""

    fun toXmlRepresentation()
            = CandidateProfile(
            executiveSummary = if(samtykke.sammendrag) cv.sammendrag else ikkeSamtykket,
            employmentHistory = EmploymentHistoryConverter(cv, samtykke).toXmlRepresentation(),
            educationHistory = EducationHistoryConverter(cv, samtykke).toXmlRepresentation(),
            licenses = LicensesConverter(cv, samtykke).toXmlRepresentation(),
            certifications = CertificationConverter(cv, samtykke).toXmlRepresentation(),
            personQualifications = PersonQualificationsConverter(cv, profile, samtykke).toXmlRepresentation()
    )
}