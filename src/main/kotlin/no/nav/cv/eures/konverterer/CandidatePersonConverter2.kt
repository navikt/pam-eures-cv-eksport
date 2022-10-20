package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Ferdighetsnivaa
import no.nav.arbeid.cv.avro.Spraakferdighet
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.cv.CvEndretInternLanguage
import no.nav.cv.eures.konverterer.country.NationalityConverter
import no.nav.cv.eures.konverterer.language.LanguageConverter
import no.nav.cv.eures.model.*
import no.nav.cv.eures.samtykke.Samtykke
import org.slf4j.LoggerFactory

class CandidatePersonConverter2(
        private val dto : CvEndretInternDto,
        private val samtykke: Samtykke
) {

    companion object {
        val log = LoggerFactory.getLogger(CandidatePersonConverter2::class.java)
    }

    fun toXmlRepresentation(): CandidatePerson {
        if (!samtykke.personalia) {
            CandidatePersonConverter.log.warn("Sharing CV without personalia, CvId : \"${dto.cv?.uuid}\"")
            return CandidatePerson(
                personName = Name(
                    givenName = "",
                    familyName = ""
                ),
                communication = listOf(),
                residencyCountryCode = "", //TODO cv.land?.toIso3166_1a2CountryCode(),
                nationalityCode = null, // quick fix issues 14. sept 2021 cv.nasjonalitet?.let { listOf(it.toIso3166_1a2CountryCode()) },
                birthDate = "",
                genderCode = GenderCode.NotSpecified,
                primaryLanguageCode = if (samtykke.spraak) getForstespraak(dto.cv?.languages) else listOf()
            )
        }
        return CandidatePerson(
            personName = Name(
                givenName = dto.personalia?.fornavn.orEmpty(),
                familyName = dto.personalia?.etternavn.orEmpty(),
            ),
            communication = Communication.buildList(
                telephone = dto.personalia?.telefon,
                email = dto.personalia?.epost,
                address = dto.personalia?.gateadresse,
                zipCode = dto.personalia?.postnummer,
                city = dto.personalia?.poststed,
                countryCode = ""), // TODO cv.land?.toIso3166_1a2CountryCode()),

            residencyCountryCode = "", //TODO cv.land?.toIso3166_1a2CountryCode(), // May be null, but that seems to be OK
            nationalityCode =  null, //cv.nasjonalitet?.let { listOf(it.toIso3166_1a2CountryCode()) },
            birthDate = dto.personalia?.foedselsdato.toString(),
            genderCode = GenderCode.NotSpecified, // TODO : Vi har vel ikke kjønn?
            primaryLanguageCode = if (samtykke.spraak) getForstespraak(dto.cv?.languages) else listOf()
        )
    }

    private fun getForstespraak(languagelist : List<CvEndretInternLanguage>?) : List<String>{
        val languages = languagelist?.filter { Ferdighetsnivaa.valueOf(it.oralProficiency) ==  Ferdighetsnivaa.FOERSTESPRAAK
                || Ferdighetsnivaa.valueOf(it.writtenProficiency) == Ferdighetsnivaa.FOERSTESPRAAK }
            ?.mapNotNull { it.iso3Code?.let { i3k -> LanguageConverter.fromIso3ToIso1(i3k)} }.orEmpty()
        return languages
    }

}