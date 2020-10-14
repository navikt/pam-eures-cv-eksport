package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Melding
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.RawCV
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton


interface CvRecordRetriever {
    fun getCvDTO(foedselsnummer: String) : Melding?
}

@Singleton
class CvAvroFromRepo(
        private val cvRepository: CvRepository,
        private val cvAvroSchema: CvAvroSchema
)  : CvRecordRetriever {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CvAvroFromRepo::class.java)
    }

    fun RawCV.toMelding() : Melding {
        val wireBytes = getWireBytes()

        val avroBytes = wireBytes.slice(5 until wireBytes.size).toByteArray()

        log.info("There is ${avroBytes.size} avro bytes for $foedselsnummer")

        val datumReader = SpecificDatumReader<Melding>(Melding::class.java)
        val decoder = DecoderFactory.get().binaryDecoder(avroBytes, null)

        return datumReader.read(null, decoder)
    }

    override fun getCvDTO(foedselsnummer: String): Melding? {
        val rawCV = cvRepository.hentCvByFoedselsnummer(foedselsnummer)
        return rawCV?.toMelding()
    }
}
