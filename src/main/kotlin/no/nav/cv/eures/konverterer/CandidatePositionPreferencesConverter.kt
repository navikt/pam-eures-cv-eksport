package no.nav.cv.eures.konverterer

import no.nav.cv.eures.model.CandidatePositionPreferences
import no.nav.cv.eures.model.PreferredLocation
import no.nav.cv.eures.model.ReferenceLocation
import no.nav.cv.eures.samtykke.Samtykke

class CandidatePositionPreferencesConverter(
    private val samtykke : Samtykke
) {

    fun toXmlRepresentation() : CandidatePositionPreferences? {
        return samtykke?.land?.toPositionPreferences()
    }

    private fun List<String>.toPositionPreferences() : CandidatePositionPreferences =
        CandidatePositionPreferences(map{countryCode -> PreferredLocation(ReferenceLocation(countryCode))})
}