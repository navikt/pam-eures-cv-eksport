package no.nav.cv.eures.scheduled

import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.cv.RawCV
import no.nav.cv.eures.cv.RawCV.Companion.RecordType.DELETE
import no.nav.cv.eures.konverterer.CvConverterService
import no.nav.cv.eures.konverterer.CvNotConvertedException
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Profile("!test")
@Service
class MessageProcessor(
        private val cvRepository: CvRepository,
        private val cvXmlRepository: CvXmlRepository,
        private val samtykkeRepository: SamtykkeRepository,
        private val cvConverterService: CvConverterService
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(MessageProcessor::class.java)
    }

    private fun List<RawCV>.processRecords(): List<RawCV> =
            partition { rawCv -> rawCv.meldingstype == DELETE }
                    .let {
                        val (deleted, createdOrModified) = it
                        listOf(
                                deleted.let { rawCvs ->
                                    rawCvs.forEach { rawCv -> cvConverterService.delete(rawCv.foedselsnummer) }
                                    rawCvs
                                },
                                createdOrModified.let { rawCvs ->
                                    val chunks = rawCvs.chunked(1000)
                                    val unsuccessfullyProcessed = mutableListOf<String>()
                                    for (chunk in chunks) {
                                        val foedselsnummer = chunk.map(RawCV::foedselsnummer)

                                        // Create new ones where Samtykke exists
                                        samtykkeRepository.hentSamtykkeUtenNaaverendeXml(foedselsnummer)
                                                .forEach { samtykke ->
                                                    log.debug("Inside hentSamtykkeUtenNaaverendeXml loop for $samtykke")
                                                    try {
                                                        cvConverterService.createOrUpdate(samtykke.foedselsnummer)
                                                    } catch (e: CvNotConvertedException) {
                                                        log.error("Failed to produce XML for fn starting with in ${samtykke.foedselsnummer.take(2)}. Continuing loop.")
                                                        unsuccessfullyProcessed.add(samtykke.foedselsnummer)
                                                    }
                                                }

                                        // Update existing ones
                                        cvXmlRepository.fetchAllActiveCvsByFoedselsnummer(foedselsnummer)
                                                .forEach { cvXml ->
                                                    try {
                                                        cvConverterService.updateExisting(cvXml)
                                                    } catch (e: CvNotConvertedException) {
                                                        log.error("Failed to update XML for fn starting with in ${cvXml.foedselsnummer.take(2)}. Continuing loop.")
                                                        unsuccessfullyProcessed.add(cvXml.foedselsnummer)
                                                    }
                                                }
                                    }
                                    rawCvs.filterNot { cv -> cv.foedselsnummer in unsuccessfullyProcessed }
                                }
                        ).flatten()
                    }


    @Scheduled(fixedDelay = 5000)
    fun process() = cvRepository.hentUprosesserteCver()
            .processRecords()
            .also { rawCvs ->
                rawCvs.map { rawCv ->
                    rawCv.prosessert = true
                    cvRepository.saveAndFlush(rawCv)
                }
                if (rawCvs.isNotEmpty()) log.info("Prosesserte ${rawCvs.size} endringer")
            }
}

