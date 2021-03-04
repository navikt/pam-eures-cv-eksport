package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.*
import no.nav.cv.eures.konverterer.esco.JanzzService
import no.nav.cv.eures.konverterer.language.LanguageConverter
import no.nav.cv.eures.model.PersonCompetency
import no.nav.cv.eures.model.PersonQualifications
import no.nav.cv.eures.samtykke.Samtykke
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

class PersonQualificationsConverter (
        private val cv: Cv,
        private val profile: Jobbprofil?,
        private val samtykke: Samtykke,
        private val janzzService: JanzzService = JanzzService.instance()
) {
    private val log: Logger = LoggerFactory.getLogger(PersonQualificationsConverter::class.java)

    fun toXmlRepresentation() : PersonQualifications? {
        val qualifications = mutableListOf<PersonCompetency>()

        if(samtykke.spraak && cv.spraakferdigheter != null)
            qualifications.addAll(cv.spraakferdigheter.toLanguages())

        if(samtykke.annenErfaring && cv.annenErfaring != null)
            qualifications.addAll(cv.annenErfaring.toEsco())

        if(samtykke.kompetanser && profile?.kompetanser != null)
            qualifications.addAll(profile.kompetanser.toEsco())

        return if(qualifications.isNotEmpty()) PersonQualifications(qualifications) else null
    }

    private fun List<Spraakferdighet>.toLanguages()  : List<PersonCompetency>
            = also { log.info("Det er ${it.size} Spraakferdighet") }
            .mapNotNull { spraak ->
                val iso1 = spraak.iso3kode?.let { i3k -> LanguageConverter.fromIso3ToIso1(i3k) }
                        ?: return@mapNotNull null

                PersonCompetency(competencyID = iso1, taxonomyID = "language") }
            .also { log.info("Mappet til ${it.size} språkferdigheter") }


    @JvmName("toEscoAnnenErfaring")
    private fun List<AnnenErfaring>.toEsco() : List<PersonCompetency>
            = also {log.info("Det er ${it.size} AnnenErfaring")}
            .mapNotNull { erfaring -> erfaring.beskrivelse?.let {janzzService.getEscoForCompetence(it) } }
            .flatten()
            .map { PersonCompetency(competencyID = it.esco, taxonomyID = "other") }
            .also { log.info("AnnenErfaring returned ${it.size} ESCO codes") }

    @JvmName("toEscoKompetanser")
    private fun List<String>.toEsco() : List<PersonCompetency>
            = also {log.info("Det er ${it.size} kompetanser")}
            .map { janzzService.getEscoForCompetence(it) }
            .flatten()
            .map { PersonCompetency(competencyID = it.esco, taxonomyID = "other") }
            .also { log.info("kompetanser returned ${it.size} ESCO codes") }

    @JvmName("toEscoFagdokumentasjon")
    private fun List<Fagdokumentasjon>.toEsco() : List<PersonCompetency>
            =  also {log.info("Det er ${it.size} fagdokumentasjoner: $it")}
            .mapNotNull { fag -> fag.tittel?.let {janzzService.getEscoForCompetence(it) } }
            .flatten()
            .map { PersonCompetency(competencyID = it.esco, taxonomyID = "other") }
            .also { log.info("Fagdokumentasjon returned ${it.size} ESCO codes") }
}
