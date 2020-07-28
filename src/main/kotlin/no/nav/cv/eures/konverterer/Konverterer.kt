package no.nav.cv.eures.konverterer

import io.micronaut.scheduling.annotation.Scheduled
import no.nav.cv.eures.cv.CvAvroSchemaClient
import no.nav.cv.eures.cv.CvRepository
import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DecoderFactory
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.nio.ByteBuffer
import javax.inject.Singleton

@Singleton
class Konverterer (
        private val cvRepository: CvRepository,
        private val schemaClient: CvAvroSchemaClient
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
        val record = regenerateAvro(aktoerId)

        log.info("AVRO :$aktoerId : $record")
    }

    private fun regenerateAvro(aktoerId: String) : GenericRecord {
        val rawCV = cvRepository.hentCv(aktoerId)
                ?: throw Exception("Prøver å konvertere CV for aktør $aktoerId, men finner den ikke i databasen.")

        val rawAvroBytes = rawCV.getAvroBytes()

        val schemaVersionuffer = ByteBuffer.wrap(rawAvroBytes.slice(1..4).toByteArray())
        val schemaVersion = schemaVersionuffer.getInt(0)
        val schema = schemaClient.getSchema(schemaVersion)

        val avroBytes = rawAvroBytes.slice(5 until rawAvroBytes.size).toByteArray()

        log.info("There is ${avroBytes.size} avro bytes")
        log.debug("SCHEMA: $schema")

        val avroSchema = Schema.Parser().parse(schema)
        val datumReader = GenericDatumReader<GenericRecord>(avroSchema)
        val decoder = DecoderFactory.get().binaryDecoder(avroBytes, null)

        val avroCv = datumReader.read(null, decoder)

        return avroCv
    }
}