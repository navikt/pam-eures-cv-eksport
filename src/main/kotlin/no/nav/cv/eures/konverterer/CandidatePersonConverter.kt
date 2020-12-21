package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Spraakferdighet
import no.nav.cv.eures.konverterer.country.NationalityConverter
import no.nav.cv.eures.konverterer.language.LanguageConverter
import no.nav.cv.eures.model.*
import no.nav.cv.eures.samtykke.Samtykke
import org.slf4j.LoggerFactory

class CandidatePersonConverter(
        private val cv: Cv,
        private val samtykke: Samtykke
) {

    companion object {
        val log = LoggerFactory.getLogger(CandidatePersonConverter::class.java)
    }

    fun toXmlRepresentation(): CandidatePerson {
        if (!samtykke.personalia) {
            log.warn("Sharing CV without personalia, CvId : \"${cv.cvId}\"")
            return CandidatePerson(
                    personName = Name(
                            givenName = "",
                            familyName = ""
                    ),
                    communication = listOf(),
                    residencyCountryCode = cv.land ?: "",
                    nationalityCode = listOf(cv.nasjonalitet?.toIso3166_1a2CountryCode() ?: ""),
                    birthDate = "",
                    genderCode = GenderCode.NotSpecified,
                    primaryLanguageCode = cv.spraakferdigheter.toLanguages()
            )
        }
        return CandidatePerson(
                personName = Name(
                        givenName = cv.fornavn,
                        familyName = cv.etternavn),

                communication = Communication.buildList(telephone = cv.telefon, email = cv.epost),

                residencyCountryCode = cv.land ?: "",
                nationalityCode = listOf(cv.nasjonalitet?.toIso3166_1a2CountryCode() ?: ""),
                birthDate = cv.foedselsdato.toString(),
                genderCode = GenderCode.NotSpecified, // TODO : Vi har vel ikke kjønn?
                primaryLanguageCode = cv.spraakferdigheter.toLanguages()
        )
    }

    private fun List<Spraakferdighet>.toLanguages() : List<String> {
        val languages = mapNotNull { LanguageConverter.fromIso3ToIso1(it.iso3kode) }
        if (languages.isEmpty()) log.warn("Missing at least one language for CvId : \"${cv.cvId}\"")
        return if(samtykke.spraak) languages else listOf()
    }

    private fun String.toIso3166_1a2CountryCode(): String {
        return NationalityConverter.getIsoCode(this)
                ?: "".also { log.warn("Cannot find nationality code for $this CvId : \"${cv.cvId}") }
    }

}