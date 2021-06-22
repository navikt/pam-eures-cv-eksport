package no.nav.cv.eures.scheduled

import no.nav.cv.eures.konverterer.CvConverterService
import no.nav.cv.eures.konverterer.esco.JanzzCacheRepository
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Profile("!test")
@Service
class XmlUpdater (
        private val cvConverterService: CvConverterService,
        private val samtykkeRepository: SamtykkeRepository,
        private val janzzCacheRepository: JanzzCacheRepository
){

    private val log: Logger = LoggerFactory.getLogger(XmlUpdater::class.java)

    @Scheduled(cron = "0 0 4 * * *") // Reprocess all CVs at 04:00 in the night
    fun updateXmlCv() {
        log.info("Pruning ESCO JANZZ cache")
        janzzCacheRepository.pruneCache()

        log.info("Reprocessing CVs to XML")
        val foedselsnumre = samtykkeRepository.finnFoedselsnumre()

        foedselsnumre
                .also { log.info("Regenerating ${it.size} XML CVs") }
                .forEach { cvConverterService.createOrUpdate(it) }

        log.info("Done reprocessing CVs")
    }
}