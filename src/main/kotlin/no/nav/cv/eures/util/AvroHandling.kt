package no.nav.cv.eures.util

import no.nav.arbeid.cv.avro.Melding
import no.nav.cv.eures.cv.CvConsumer
import no.nav.cv.eures.cv.KafkaConfig
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.slf4j.LoggerFactory


class AvroHandling {
}

// Julian:
// This is definitely not the best solution, but unfortunately
// it's the only one I see for handling both avro versions without
// ending up in situations where we re-index things and get errors
// until we finish processing the first part of the topic.
fun ByteArray.toMelding(aktorId: String): Melding {
    val log = LoggerFactory.getLogger(AvroHandling::class.java)

    // NOTE: The newest AVRO version prefixes 6 bytes instead of 4

    return try {
        readDatum(aktorId = aktorId, avroPrefixByteSize = 5)
    } catch (e: Exception) {
        log.warn("Failed to decode kafka message for $aktorId in the first try, retrying. Size: $size", e)
        readDatum(aktorId = aktorId, avroPrefixByteSize = 7)
    } catch (e: Exception) {
        log.error("Klarte ikke decode kafka melding for $aktorId. Size: $size", e)
        throw(e)
    }
}

private fun ByteArray.readDatum(aktorId: String, avroPrefixByteSize: Int): Melding {
    val log = LoggerFactory.getLogger(AvroHandling::class.java)

    if(size < avroPrefixByteSize)
        throw Exception("Trying to decode a message of only $size bytes, with a prefix of $avroPrefixByteSize")

    try {
        val datumReader = SpecificDatumReader(Melding::class.java)

        // TODO - Figure out if there's away to avoid this.
        val businessPartOfMessage = slice(avroPrefixByteSize until size).toByteArray()

        val decoder = DecoderFactory.get().binaryDecoder(businessPartOfMessage, null)
        return datumReader.read(null, decoder)
    } catch (e: Exception) {
        log.warn("Klarte ikke å deserialisere avromeldingen til $aktorId med versjon prefiks på: $avroPrefixByteSize bytes og total størrelse: $size", e)
        throw e
    }
}
