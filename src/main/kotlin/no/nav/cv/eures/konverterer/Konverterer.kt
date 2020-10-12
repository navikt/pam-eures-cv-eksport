package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.cv.eures.cv.CvXml
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import javax.inject.Singleton

@Singleton
open class Konverterer(
        private val cvRecordRetriever: CvRecordRetriever,
        private val cvXmlRepository: CvXmlRepository,
        private val samtykkeRepository: SamtykkeRepository
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(Konverterer::class.java)
    }

    fun oppdaterEksisterende(cvXml: CvXml?): CvXml? {
        val now = ZonedDateTime.now()
        return cvXml?.let {
            konverterTilXML(it.aktoerId).let { xml ->
                it.sistEndret = now
                it.slettet = null
                it.xml = xml.second
                return cvXmlRepository.save(it)
            }
        }
    }

    fun oppdaterEllerLag(aktoerId: String) {
        val now = ZonedDateTime.now()
        cvXmlRepository.fetch(aktoerId)?.let { oppdaterEksisterende(it) }
            ?: konverterTilXML(aktoerId).let {
                cvXmlRepository.save(CvXml.create(
                        reference = it.first,
                        aktoerId = aktoerId,
                        opprettet = now,
                        sistEndret = now,
                        slettet = null,
                        xml = it.second
                ))
            }
    }

    fun slett(aktoerId: String): CvXml? = cvXmlRepository.fetch(aktoerId)
            ?.let {
                it.slettet = if (it.slettet != null) it.slettet else ZonedDateTime.now()
                it.xml = ""
                return@let cvXmlRepository.save(it)
            }

    fun konverterTilXML(aktoerId: String): Pair<String, String> {
        val record = cvRecordRetriever.getCvDTO(aktoerId)

        val cv = when (record.meldingstype) {
            Meldingstype.OPPRETT -> record.opprettCv.cv
            Meldingstype.ENDRE -> record.endreCv.cv
            else -> throw Exception("Ukjent meldingstype: " + record.get("meldingstype"))
        }

        log.debug("Firstname : ${cv.fornavn}")


        val samtykke = samtykkeRepository.hentSamtykke(aktoerId)
        // ?: Samtykke(aktoerId = aktoerId, sistEndret = ZonedDateTime.now(), utdanning = true, arbeidserfaring = true)
                ?: throw Exception("Aktoer $aktoerId har ikke gitt samtykke")

        val candidate = CandidateConverter(cv, samtykke).toXmlRepresentation()

        return Pair(cv.arenaKandidatnr, XmlSerializer.serialize(candidate))
    }

}
