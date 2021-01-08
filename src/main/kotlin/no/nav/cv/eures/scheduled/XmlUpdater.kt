package no.nav.cv.eures.scheduled

import no.nav.cv.eures.konverterer.CvConverterService
import no.nav.cv.eures.konverterer.esco.JanzzCacheRepository
import no.nav.cv.eures.konverterer.esco.JanzzService
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

    @Scheduled(fixedDelay = 1000 * 60)
    fun updateXmlCv() {
        log.info("Pruning ESCO JANZZ cache")
        janzzCacheRepository.pruneCache()


        val foedselsnumre = samtykkeRepository.finnFoedselsnumre()

        foedselsnumre
                .also { log.info("Regenerating ${it.size} origin rate XML") }
                .forEach { cvConverterService.createOrUpdate(it) }
    }
}