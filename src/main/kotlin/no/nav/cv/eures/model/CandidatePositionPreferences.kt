package no.nav.cv.eures.model

class CandidatePositionPreferences(
    val preferredLocations: List<PreferredLocation>
)

class PreferredLocation(
    val referenceLocation: ReferenceLocation
)

class ReferenceLocation(
    val countryCode: String
)