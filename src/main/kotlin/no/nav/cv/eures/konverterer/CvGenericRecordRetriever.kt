package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.cv.eures.cv.CvRepository
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.slf4j.LoggerFactory
import java.lang.Exception
import javax.inject.Singleton


interface CvGenericRecordRetriever {
    fun getCvJavaObject(aktoerId: String) : Cv
}

@Singleton
class CvAvroFromRepo(
        private val cvRepository: CvRepository,
        private val cvAvroSchema: CvAvroSchema
)  : CvGenericRecordRetriever {

    companion object {
        val log = LoggerFactory.getLogger(CvAvroFromRepo::class.java)
    }

    override fun getCvJavaObject(aktoerId: String): Cv {
        val rawCV = cvRepository.hentCv(aktoerId)
                ?: throw Exception("Prøver å konvertere CV for aktør $aktoerId, men finner den ikke i databasen.")

        val wireBytes = rawCV.getWireBytes()

        val avroBytes = wireBytes.slice(5 until wireBytes.size).toByteArray()

        log.info("There is ${avroBytes.size} avro bytes for $aktoerId")

        val datumReader = SpecificDatumReader<Cv>(Cv::class.java)
        val decoder = DecoderFactory.get().binaryDecoder(avroBytes, null)

        val avroCv = datumReader.read(null, decoder)

        return avroCv
    }
}

//@Singleton
//class CvGenericRecordFromRepo(
//        private val cvRepository: CvRepository,
//        private val cvAvroSchema: CvAvroSchema
//)  : CvGenericRecordRetriever {
//
//    companion object {
//        val log = LoggerFactory.getLogger(CvGenericRecordFromRepo::class.java)
//    }
//    override fun getCvGenericRecord(aktoerId: String) : GenericRecord {
//        val rawCV = cvRepository.hentCv(aktoerId)
//                ?: throw Exception("Prøver å konvertere CV for aktør $aktoerId, men finner den ikke i databasen.")
//
//        val wireBytes = rawCV.getWireBytes()
//
//        val schema = cvAvroSchema.getSchema(wireBytes)
//
//        val avroBytes = wireBytes.slice(5 until wireBytes.size).toByteArray()
//
//        log.info("There is ${avroBytes.size} avro bytes for $aktoerId")
//
//        val datumReader = GenericDatumReader<GenericRecord>(schema)
//        val decoder = DecoderFactory.get().binaryDecoder(avroBytes, null)
//
//        val avroCv = datumReader.read(null, decoder)
//
//        return avroCv
//    }
//}