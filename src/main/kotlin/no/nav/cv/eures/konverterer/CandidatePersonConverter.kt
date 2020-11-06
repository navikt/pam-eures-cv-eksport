package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Spraakferdighet
import no.nav.cv.eures.model.*
import no.nav.cv.eures.samtykke.Samtykke

class CandidatePersonConverter(
        private val cv: Cv,
        private val samtykke: Samtykke
) {
    fun toXmlRepresentation() = when (samtykke.personalia) {
        false -> throw Exception("Personalia is mandatory at this stage") // TODO : Consider how to handle when people don't want to share personalia
        true -> CandidatePerson(
                personName = Name(
                        givenName = cv.fornavn,
                        familyName = cv.etternavn),

                communication = Communication.buildList(telephone = cv.telefon, email = cv.epost),

                residencyCountryCode = cv.land.toIso3166_1a2CountryCode(),
                nationalityCode = listOf(cv.nasjonalitet.toIso3166_1a2CountryCode()),
                birthDate = cv.foedselsdato.toString(),
                genderCode = GenderCode.NotSpecified, // TODO : Vi har vel ikke kjønn?
                primaryLanguageCode = cv.spraakferdigheter.toLanguages())
    }

    private fun List<Spraakferdighet>.toLanguages() = map { it.iso3kode.toIso639_1() }

    // TODO Implement ISO639-3 to ISO693-1 conversion
    private fun String.toIso639_1() = this

    private fun String.toIso3166_1a2CountryCode() = this

}