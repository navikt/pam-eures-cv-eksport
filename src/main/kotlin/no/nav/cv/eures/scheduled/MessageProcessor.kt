package no.nav.cv.eures.scheduled

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.konverterer.CvRecordRetriever
import no.nav.cv.eures.konverterer.Konverterer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Requires(notEnv = ["test"])
@Singleton
class MessageProcessor(
        private val cvRecordRetriever: CvRecordRetriever,
        private val cvRepository: CvRepository,
        private val cvXmlRepository: CvXmlRepository,
        private val konverterer: Konverterer
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(MessageProcessor::class.java)
    }

    @Scheduled(fixedDelay = "5s")
    fun process() {
        // TODO - Make use of batches.
        val endringer = cvRecordRetriever.getUnprocessedCvDTOs()
                .partition { it.second.meldingstype == Meldingstype.SLETT }
                .let {
                    val (deletedPairs, createdOrModifiedPairs) = it
                    val deleted = deletedPairs.map { pair -> pair.first }.map { rawCv ->
                        konverterer.slett(rawCv.foedselsnummer)
                        return@map rawCv
                    }
                    // TODO - Implement query logic through code?
                    val createdOrModified = createdOrModifiedPairs.map { pair -> pair.first }.let{ rawCvs ->
                        cvXmlRepository.fetchAllActiveCvsByFoedselsnummer(rawCvs.map { rawCv -> rawCv.foedselsnummer }).forEach { cv ->
                            konverterer.oppdaterEksisterende(cv)
                        }
                        rawCvs
                    }
                    return@let listOf(deleted, createdOrModified).flatten()
                            .map { rawCv -> cvRepository.lagreCv(rawCv.also { record -> record.prosessert = true }) }
                }
        endringer.size.let {
            if (it > 0) log.info("Prosesserte $it endringer")
        }
    }
}
