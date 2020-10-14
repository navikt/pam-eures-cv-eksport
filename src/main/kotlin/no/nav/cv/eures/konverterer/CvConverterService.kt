package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Melding
import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.CvXml
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.cv.RawCV
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import javax.inject.Singleton

@Singleton
open class CvConverterService(
        private val cvRepository: CvRepository,
        private val cvXmlRepository: CvXmlRepository,
        private val samtykkeRepository: SamtykkeRepository
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CvConverterService::class.java)
    }


    private fun RawCV.toMelding() : Melding? {
        val wireBytes = getWireBytes()

        if (wireBytes.isEmpty()) return null

        val avroBytes = wireBytes.slice(5 until wireBytes.size).toByteArray()

        CvAvroFromRepo.log.info("There is ${avroBytes.size} avro bytes for $foedselsnummer")

        val datumReader = SpecificDatumReader<Melding>(Melding::class.java)
        val decoder = DecoderFactory.get().binaryDecoder(avroBytes, null)

        return datumReader.read(null, decoder)
    }

    private fun Melding.cv() : Cv? = when(meldingstype) {
        Meldingstype.OPPRETT -> opprettCv.cv
        Meldingstype.ENDRE -> endreCv.cv
        else -> null
    }

    fun updateExisting(cvXml: CvXml?): CvXml? {
        val now = ZonedDateTime.now()
        return cvXml?.let {
            convertToXml(it.foedselsnummer)?.let { xml ->
                it.sistEndret = now
                it.slettet = null
                it.xml = xml.second
                return cvXmlRepository.save(it)
            }
        }
    }

    fun createOrUpdate(foedselsnummer: String) {
        val now = ZonedDateTime.now()
        cvXmlRepository.fetch(foedselsnummer)?.let { updateExisting(it) }
            ?: convertToXml(foedselsnummer)?.let {
                cvXmlRepository.save(CvXml.create(
                        reference = it.first,
                        aktoerId = foedselsnummer,
                        opprettet = now,
                        sistEndret = now,
                        slettet = null,
                        xml = it.second
                ))
            }
    }

    fun delete(foedselsnummer: String): CvXml? = cvXmlRepository.fetch(foedselsnummer)
            ?.let {
                it.slettet = it.slettet ?: ZonedDateTime.now()
                it.xml = ""
                samtykkeRepository.slettSamtykke(foedselsnummer)
                return@let cvXmlRepository.save(it)
            }

    fun convertToXml(foedselsnummer: String): Pair<String, String>? {
        val record = cvRepository.hentCvByFoedselsnummer(foedselsnummer) ?: return null
        return record.toMelding()?.cv()?.let { cv ->

            log.debug("Firstname : ${cv.fornavn}")

            val samtykke = samtykkeRepository.hentSamtykke(foedselsnummer)
                    ?: throw Exception("Aktoer $foedselsnummer har ikke gitt samtykke")

            val candidate = CandidateConverter(cv, samtykke).toXmlRepresentation()

            return@let Pair(cv.arenaKandidatnr, XmlSerializer.serialize(candidate))
        }

    }

}
