package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

// 4.11
data class CandidateProfile(
        val candidatePositionPreferences: CandidatePositionPreferences? = null,
        @JacksonXmlProperty(isAttribute = true, localName = "languageCode")
        val languageCode: String = "no",
        val executiveSummary: String = "",
        val employmentHistory: EmploymentHistory? = null,
        val educationHistory: EducationHistory? = null,
        val licenses: Licenses? = null,
        val certifications: Certifications? = null,
        val personQualifications: PersonQualifications? = null
)