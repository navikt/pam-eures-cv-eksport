package no.nav.cv.eures.scheduled

import io.micrometer.influx.InfluxMeterRegistry
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.eures.EuresService
import no.nav.cv.eures.konverterer.esco.JanzzCacheRepository
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Profile("!test")
@Service
class GenerateMetrics(
        private val promMeterRegistry: PrometheusMeterRegistry,
        private val influxMeterRegistry: InfluxMeterRegistry,
        private val samtykkeRepository: SamtykkeRepository,
        private val cvRepository: CvRepository,
        private val cvXmlRepository: CvXmlRepository,
        private val euresService: EuresService,
        private val janzzCacheRepository: JanzzCacheRepository
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(GenerateMetrics::class.java)
    }

    @Scheduled (fixedDelay = 1000 * 60)
    fun count() {
        val count = samtykkeRepository.hentAntallSamtykker()
        promMeterRegistry.gauge("cv.eures.eksport.antall.samtykker.total", count)
        log.info("Metric: $count samtykker er hentet")

        val countRaw = cvRepository.fetchCountRawCvs()
        promMeterRegistry.gauge("cv.eures.eksport.antall.raw.total", countRaw)
        log.info("Metric:$countRaw RawCV-er er hentet")

        val countExportable = cvXmlRepository.fetchAllActive().size
        promMeterRegistry.gauge("cv.eures.eksport.antall.exportable.total", countExportable)
        log.info("Metric:$countExportable eksporterbare CV-er er hentet")

        val countDeletable = cvXmlRepository.fetchCountDeletableCvs()
        promMeterRegistry.gauge("cv.eures.eksport.antall.deletable.total", countDeletable)
        log.info("Metric:$countDeletable slettbare CV-er er hentet")

        val (created, modified, closed) = euresService.getAll()
        promMeterRegistry.gauge("cv.eures.eksport.antall.euresService.created.total", created.size)
        promMeterRegistry.gauge("cv.eures.eksport.antall.euresService.modified.total", modified.size)
        promMeterRegistry.gauge("cv.eures.eksport.antall.euresService.closed.total", closed.size)
        log.info("Metric: ${created.size} opprettet, ${modified.size} endret, ${closed.size} slettet")

        val countEscoCache = janzzCacheRepository.getCacheCount()
        promMeterRegistry.gauge("cv.eures.eksport.antall.escoCache.total", countEscoCache)
        log.info("Metric: $countEscoCache linjer i ESCO cache")

        val influxCreated = influxMeterRegistry.counter("cv.eures.eksport.samtykke.opprettet").count()
        promMeterRegistry.gauge("cv.eures.eksport.influx.samtykke.opprettet", influxCreated)
        log.info("Metric: $influxCreated Influx opprettet")

        val influxDeleted = influxMeterRegistry.counter("cv.eures.eksport.samtykke.slettet").count()
        promMeterRegistry.gauge("cv.eures.eksport.influx.samtykke.slettet", influxDeleted)
        log.info("Metric: $influxDeleted Influx slettet")


    }
}