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
            employmentHistory = EmploymentHistoryConverter(cv, samtykke).toXmlRepresentation(),
            educationHistory = EducationHistoryConverter(cv, samtykke).toXmlRepresentation()
    )
}