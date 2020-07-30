package no.nav.cv.eures.konverterer

import io.micronaut.scheduling.annotation.Scheduled
import no.nav.cv.eures.cv.CvRepository
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DecoderFactory
import org.slf4j.LoggerFactory
import java.lang.Exception
import javax.inject.Singleton

@Singleton
class Konverterer (
    private val cvGenericRecordRetriever: CvGenericRecordRetriever
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
        val record = cvGenericRecordRetriever.getCvGenericRecord(aktoerId)

        log.info("AVRO :$aktoerId : $record")
    }
}