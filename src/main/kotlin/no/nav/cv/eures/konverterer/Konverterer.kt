package no.nav.cv.eures.konverterer

import io.micronaut.scheduling.annotation.Scheduled
import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.cv.eures.model.*
import no.nav.cv.eures.samtykke.Samtykke
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Singleton

@Singleton
class Konverterer (
        private val cvRecordRetriever: CvRecordRetriever,
        private val samtykkeRepository: SamtykkeRepository
) {

    companion object {
        val log = LoggerFactory.getLogger(Konverterer::class.java)
    }

    fun oppdater(aktoerId: String) {

    }

    fun konverterTilXML(aktoerId: String) : String {




       return "Nothing"
    }

    @Scheduled(fixedDelay = "5s")
    fun testing() {

        val aktoerId = "10013106889"
        val record = cvRecordRetriever.getCvDTO(aktoerId)


        val cv = when(record.meldingstype) {
            Meldingstype.OPPRETT -> record.opprettCv.cv
            Meldingstype.ENDRE -> record.endreCv.cv
            else -> throw Exception("Ukjent meldingstype: " + record.get("meldingstype"))
        }

        log.debug("Firstname : ${cv.fornavn}")


        val samtykke = samtykkeRepository.hentSamtykke(aktoerId)
                ?: Samtykke(aktoerId = aktoerId, sistEndret = ZonedDateTime.now(), utdanning = true, arbeidserfaring = true)
                ?: throw Exception("Aktoer $aktoerId har ikke gitt samtykke")

        val candidate = CandidateConverter(cv, samtykke).toXmlRepresentation()

        log.info("AktoerID :$aktoerId")
        log.info("Record :$record")
        log.info("Candidate :$candidate")
        log.info("XML: ${XmlSerializer.serialize(candidate)}")
    }
}