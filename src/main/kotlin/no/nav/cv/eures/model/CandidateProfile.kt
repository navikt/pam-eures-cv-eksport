package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

// 4.11
data class CandidateProfile(
        @JacksonXmlProperty(isAttribute = true, localName = "languageCode")
        val languageCode: String = "en",
        val executiveSummary: String = "",
        val employmentHistory: EmploymentHistory? = null,
        val educationHistory: EducationHistory? = null,
        val licenses: Licenses? = null
)