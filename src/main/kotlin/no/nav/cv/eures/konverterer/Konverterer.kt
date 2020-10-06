package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.cv.eures.cv.CvXml
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.samtykke.Samtykke
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import javax.inject.Singleton

@Singleton
class Konverterer(
        private val cvRecordRetriever: CvRecordRetriever,
        private val cvXmlRepository: CvXmlRepository,
        private val samtykkeRepository: SamtykkeRepository
) {

    companion object {
        val log = LoggerFactory.getLogger(Konverterer::class.java)
    }

    fun oppdater(aktoerId: String) {
        val now = ZonedDateTime.now()
        cvXmlRepository.hentCv(aktoerId)
                ?.let {
                    it.sistEndret = now
                    it.xml = konverterTilXML(it.aktoerId)
                }
        ?: cvXmlRepository.lagreCvXml(
                CvXml.create(
                        aktoerId = aktoerId,
                        opprettet = now,
                        sistEndret = now,
                        slettet = null,
                        xml = konverterTilXML(aktoerId)
            )
        )
    }

    fun slett(aktoerId: String) {
        cvXmlRepository.hentCv(aktoerId)
                ?.let {
                    it.slettet = ZonedDateTime.now()
                    cvXmlRepository.lagreCvXml(it)
                }
    }

    fun konverterTilXML(aktoerId: String): String {
        val record = cvRecordRetriever.getCvDTO(aktoerId)

        val cv = when (record.meldingstype) {
            Meldingstype.OPPRETT -> record.opprettCv.cv
            Meldingstype.ENDRE -> record.endreCv.cv
            else -> throw Exception("Ukjent meldingstype: " + record.get("meldingstype"))
        }

        log.debug("Firstname : ${cv.fornavn}")


        val samtykke = samtykkeRepository.hentSamtykke(aktoerId)
                ?: Samtykke(aktoerId = aktoerId, sistEndret = ZonedDateTime.now(), utdanning = true, arbeidserfaring = true)
                ?: throw Exception("Aktoer $aktoerId har ikke gitt samtykke")

        val candidate = CandidateConverter(cv, samtykke).toXmlRepresentation()

        return XmlSerializer.serialize(candidate)
    }

}
