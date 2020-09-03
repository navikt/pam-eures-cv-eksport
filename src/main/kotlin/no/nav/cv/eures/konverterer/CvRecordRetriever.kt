package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Melding
import no.nav.cv.eures.cv.CvRepository
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.slf4j.LoggerFactory
import java.lang.Exception
import javax.inject.Singleton


interface CvRecordRetriever {
    fun getCvDTO(aktoerId: String) : Melding
}

@Singleton
class CvAvroFromRepo(
        private val cvRepository: CvRepository,
        private val cvAvroSchema: CvAvroSchema
)  : CvRecordRetriever {

    companion object {
        val log = LoggerFactory.getLogger(CvAvroFromRepo::class.java)
    }

    override fun getCvDTO(aktoerId: String): Melding {
        val rawCV = cvRepository.hentCv(aktoerId)
                ?: throw Exception("Prøver å konvertere CV for aktør $aktoerId, men finner den ikke i databasen.")

        val wireBytes = rawCV.getWireBytes()

        val avroBytes = wireBytes.slice(5 until wireBytes.size).toByteArray()

        log.info("There is ${avroBytes.size} avro bytes for $aktoerId")

        val datumReader = SpecificDatumReader<Melding>(Melding::class.java)
        val decoder = DecoderFactory.get().binaryDecoder(avroBytes, null)

        return datumReader.read(null, decoder)
    }
}