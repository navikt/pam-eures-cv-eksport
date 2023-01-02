package no.nav.cv.eures.scheduled

import no.nav.cv.eures.janzz.JanzzCacheRepository
import no.nav.cv.eures.konverterer.CvConverterService
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class XmlUpdater (
        private val cvConverterService2: CvConverterService,
        private val samtykkeRepository: SamtykkeRepository,
        private val janzzCacheRepository: JanzzCacheRepository
){

    private val log: Logger = LoggerFactory.getLogger(XmlUpdater::class.java)

    @Scheduled(cron = "0 0 2 * * *") // Reprocess all CVs at 02:00 in the night
    fun updateXmlCv() {
        log.info("Pruning ESCO JANZZ cache")
        janzzCacheRepository.pruneCache()

        log.info("Reprocessing CVs to XML")
        val foedselsnumre = samtykkeRepository.finnFoedselsnumre()

        foedselsnumre
                .also { log.info("Regenerating ${it.size} XML CVs") }
                .forEach {
                    try {
                        cvConverterService2.createOrUpdate(it)
                    } catch (e: Exception) {
                        log.warn("Failed to reprocess CV for ${it.take(1)}.........${it.takeLast(1)}: ${e.message}", e)
                    }
                }

        log.info("Done reprocessing CVs")
    }
}