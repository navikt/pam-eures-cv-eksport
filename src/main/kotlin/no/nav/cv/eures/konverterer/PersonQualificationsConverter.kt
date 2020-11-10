package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.cv.eures.model.PersonCompetency
import no.nav.cv.eures.model.PersonQualifications
import no.nav.cv.eures.samtykke.Samtykke

class PersonQualificationsConverter (
        private val cv: Cv,
        private val samtykke: Samtykke
) {
    fun toXmlRepresentation() : PersonQualifications {
        val qualifications = mutableListOf<PersonCompetency>()

        return PersonQualifications(qualifications)
    }
}