package no.nav.cv.eures.util

import no.nav.arbeid.cv.avro.Melding
import no.nav.cv.eures.cv.CvConsumer
import no.nav.cv.eures.cv.KafkaConfig
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.slf4j.LoggerFactory


class AvroHandling {
}

fun List<Byte>.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

fun ByteArray.toMelding(aktorId: String): Melding {
    val log = LoggerFactory.getLogger(AvroHandling::class.java)

    val avroPrefixByteSize = 5 // Byte 0 is magic byte, bytes 1-4 is avro schema version - BEWARE OF VERSION

    return try {
        if(size < avroPrefixByteSize)
            throw Exception("Trying to decode a message of only $size bytes, with a prefix of $avroPrefixByteSize")

        val compatibleAvroVersion = "0000031f"
        val messageAvroVersion = slice(1..4).toHex()

        if(!messageAvroVersion.equals(compatibleAvroVersion)) {
            val e = Exception("Trying to decode a message with the wrong avro version! Version is $messageAvroVersion, but should be $compatibleAvroVersion")
            log.error("Unable to decode avro message", e)
            throw e
        }

        val datumReader = SpecificDatumReader(Melding::class.java)

        // TODO - Figure out if there's away to avoid this.
        val businessPartOfMessage = slice(avroPrefixByteSize until size).toByteArray()

        val decoder = DecoderFactory.get().binaryDecoder(businessPartOfMessage, null)

        datumReader.read(null, decoder)
    } catch (e: Exception) {
        log.error("Klarte ikke decode kafka melding for $aktorId. Size: $size", e)
        throw(e)
    }
}
