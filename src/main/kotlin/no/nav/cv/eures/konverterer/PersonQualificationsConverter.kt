package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.AnnenErfaring
import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Fagdokumentasjon
import no.nav.arbeid.cv.avro.Jobbprofil
import no.nav.cv.eures.konverterer.esco.JanzzService
import no.nav.cv.eures.model.PersonCompetency
import no.nav.cv.eures.model.PersonQualifications
import no.nav.cv.eures.samtykke.Samtykke
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

class PersonQualificationsConverter (
        private val cv: Cv,
        private val profile: Jobbprofil,
        private val samtykke: Samtykke
) {
    private val log: Logger = LoggerFactory.getLogger(PersonQualificationsConverter::class.java)

    @Autowired
    private lateinit var janzzService: JanzzService

    fun toXmlRepresentation() : PersonQualifications {
        val qualifications = mutableListOf<PersonCompetency>()

        if(samtykke.annenErfaring)
            qualifications.addAll(cv.annenErfaring.toEsco())

        if(samtykke.fagbrev)
            qualifications.addAll(cv.fagdokumentasjon.toEsco())

        if(samtykke.annenErfaring)
            qualifications.addAll(profile.kompetanser.toEsco())

        return PersonQualifications(qualifications)
    }

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
