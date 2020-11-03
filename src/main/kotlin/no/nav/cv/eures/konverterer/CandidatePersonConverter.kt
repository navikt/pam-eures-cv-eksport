package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Spraakferdighet
import no.nav.cv.eures.model.*
import no.nav.cv.eures.samtykke.Samtykke

class CandidatePersonConverter (
        private val cv: Cv,
        private val samtykke: Samtykke
) {

    fun toXmlRepresentation()
            = CandidatePerson(
                personName = Name(
                        givenName = cv.fornavn,
                        familyName = cv.etternavn),

                communication = Communication.buildList(telephone = cv.telefon, mobileTelephone = cv.epost),

                residencyCountryCode = CountryCodeISO3166_Alpha_2.NO, // cv.get("land")
                nationalityCode = listOf(CountryCodeISO3166_Alpha_2.NO), // cv.get("nasjonalitet")
                birthDate = cv.foedselsdato.toString(),
                genderCode = GenderCode.NotSpecified,
                primaryLanguageCode = cv.spraakferdigheter.toLanguages())

    private fun List<Spraakferdighet>.toLanguages()
            = map { it.iso3kode.toIso639_1() }

    // TODO Implement ISO639-3 to ISO693-1 conversion
    private fun String.toIso639_1() = this
}