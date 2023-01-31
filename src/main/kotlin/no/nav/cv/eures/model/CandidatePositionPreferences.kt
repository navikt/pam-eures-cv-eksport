package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class CandidatePositionPreferences(
    val preferredLocations: List<PreferredLocation>
)

data class PreferredLocation(
    val referenceLocation: ReferenceLocation
)

data class ReferenceLocation(
    @JacksonXmlProperty(isAttribute = true, localName = "listName")
    val listName: String?= "Countries",
    @JacksonXmlProperty(isAttribute = true, localName = "listURI")
    val listURI: String?= "http://ec.europa.eu/esco/ConceptScheme/country",
    @JacksonXmlProperty(isAttribute = true, localName = "listName")
    val listVersionID: String?= "3166-1-alpha-2",
    val countryCode: String
)