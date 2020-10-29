package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.cv.eures.model.CandidateProfile
import no.nav.cv.eures.samtykke.Samtykke

class CandidateProfileConverter(
        private val cv: Cv,
        private val samtykke: Samtykke
) {
    fun toXmlRepresentation()
            = CandidateProfile(
            executiveSummary = cv.sammendrag,
            employmentHistory = EmploymentHistoryConverter(cv, samtykke).toXmlRepresentation(),
            educationHistory = EducationHistoryConverter(cv, samtykke).toXmlRepresentation(),
            licenses = LicensesConverter(cv, samtykke).toXmlRepresentation()
    )
}