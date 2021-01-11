package no.nav.cv.eures.scheduled

import io.micrometer.core.instrument.MeterRegistry
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.eures.EuresService
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Profile("!test")
@Service
class GenerateMetrics(
        private val meterRegistry: MeterRegistry,
        private val samtykkeRepository: SamtykkeRepository,
        private val cvRepository: CvRepository,
        private val cvXmlRepository: CvXmlRepository,
        private val euresService: EuresService
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(GenerateMetrics::class.java)
    }

    @Scheduled (fixedDelay = 1000 * 60)
    fun count() {
        val count = samtykkeRepository.hentAntallSamtykker()
        meterRegistry.gauge("cv.eures.eksport.antall.samtykker.total", count)
        log.info("$count samtykker er hentet")

        val countRaw = cvRepository.fetchCountRawCvs()
        log.info("$countRaw RawCV-er er hentet")

        val countExportable = cvXmlRepository.fetchAllActive().size
        log.info("$countExportable eksporterbare CV-er er hentet")

        val countDeletable = cvXmlRepository.fetchCountDeletableCvs()
        log.info("$countDeletable slettbare CV-er er hentet")

        val (created, modified, closed) = euresService.getAll()
        log.info("${created.size} opprettet, ${modified.size} endret, ${closed.size} slettet")
    }
}