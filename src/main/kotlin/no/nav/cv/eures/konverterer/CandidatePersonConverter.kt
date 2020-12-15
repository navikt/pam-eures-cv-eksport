package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Spraakferdighet
import no.nav.cv.eures.konverterer.country.NationalityConverter
import no.nav.cv.eures.konverterer.language.LanguageConverter
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

                residencyCountryCode = cv.land,
                nationalityCode = listOf(cv.nasjonalitet.toIso3166_1a2CountryCode()),
                birthDate = cv.foedselsdato.toString(),
                genderCode = GenderCode.NotSpecified, // TODO : Vi har vel ikke kjønn?
                primaryLanguageCode = cv.spraakferdigheter.toLanguages())
    }

    private fun List<Spraakferdighet>.toLanguages() : List<String> {
        val languages = mapNotNull { LanguageConverter.fromIso3ToIso1(it.iso3kode) }
        return if(languages.isNotEmpty()) languages
            else throw Exception("Cannot produce XML without at least one language")
    }

    private fun String.toIso3166_1a2CountryCode() = NationalityConverter.getIsoCode(this)
            ?: throw Exception("Cannot find nationality code for $this CvId : ${cv.cvId}") // TODO : Is this Aktør ID?

}