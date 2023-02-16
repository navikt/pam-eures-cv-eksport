package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class CandidatePositionPreferences(
    val preferredLocation: List<PreferredLocation>
)

data class PreferredLocation(
    val referenceLocation: ReferenceLocation
)

data class ReferenceLocation(
    val countryCode: String
)