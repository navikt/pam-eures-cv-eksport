package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.*
import no.nav.cv.eures.janzz.JanzzService
import no.nav.cv.eures.konverterer.language.LanguageConverter
import no.nav.cv.eures.model.PersonCompetency
import no.nav.cv.eures.model.PersonQualifications
import no.nav.cv.eures.samtykke.Samtykke
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PersonQualificationsConverter (
        private val cv: Cv,
        private val profile: Jobbprofil?,
        private val samtykke: Samtykke,
        private val janzzService: JanzzService = JanzzService.instance()
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PersonQualificationsConverter::class.java)
    }
    val debug = cv.aktoerId in listOf("2308808164824", "2672989697496", "2503811631032")

    fun toXmlRepresentation() : PersonQualifications? {
        val qualifications = mutableListOf<PersonCompetency>()

        if(samtykke.spraak && cv.spraakferdigheter != null)
            qualifications.addAll(cv.spraakferdigheter.toLanguages())

        if(samtykke.kompetanser && profile?.kompetanser != null)
            qualifications.addAll(profile.kompetanser.toEsco())

        return if(qualifications.isNotEmpty()) PersonQualifications(qualifications) else null
    }

    private fun List<Spraakferdighet>.toLanguages()  : List<PersonCompetency>
            = onEach { if(debug) log.debug("${cv.aktoerId} QUAL has language $it") }
            .mapNotNull { spraak ->
                spraak.iso3kode
                        ?.let { i3k -> LanguageConverter.fromIso3ToIso1(i3k) }
                        ?.let { PersonCompetency(competencyID = it, taxonomyID = "language") }
            }
            .onEach { if(debug) log.debug("${cv.aktoerId} QUAL got language $it") }

    @JvmName("toEscoKompetanser")
    private fun List<String>.toEsco() : List<PersonCompetency>
            = asSequence().onEach { if(debug) log.debug("${cv.aktoerId} QUAL got competance $it") }
            .map { janzzService.getEscoForTerm(it, JanzzService.EscoLookupType.SKILL) }
            .flatten()
            .map { PersonCompetency(competencyID = it.esco, taxonomyID = "other") }
            .onEach { if(debug) log.debug("${cv.aktoerId} QUAL got mapped competance $it") }.toList()

}
