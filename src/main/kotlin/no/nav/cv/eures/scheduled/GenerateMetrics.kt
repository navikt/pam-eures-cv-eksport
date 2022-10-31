package no.nav.cv.eures.scheduled

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.eures.EuresService
import no.nav.cv.eures.janzz.JanzzCacheRepository
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Profile("!test")
@Service
class GenerateMetrics(
        private val meterRegistry: MeterRegistry,
        private val samtykkeRepository: SamtykkeRepository,
        private val cvRepository: CvRepository,
        private val euresService: EuresService,
        private val janzzCacheRepository: JanzzCacheRepository
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(GenerateMetrics::class.java)
    }

    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    val gauges = mutableMapOf<String, AtomicLong>()

    private fun addOrUpdateGauge(name: String, value: Int)
        = addOrUpdateGauge(name, value.toLong())

    private fun addOrUpdateGauge(name: String, value: Long) {
        gauges[name]?.set(value)
            ?: addGauge(name, value)
    }

    private fun addGauge(name: String, value: Long) {
        gauges[name] = meterRegistry.gauge(name, AtomicLong(value))
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    fun count() {
        try {
            val count = samtykkeRepository.hentAntallSamtykker()
            addOrUpdateGauge("cv.eures.eksport.antall.samtykker.total", count)
            log.info("Metric: $count samtykker er lagret i EURES databasen")

            samtykkeRepository.hentAntallSamtykkerPerKategori()
                .also {
                    log.info("Got these categories and counts: ${it.map { (kategori, antall) -> "$kategori: $antall" }.joinToString ( "," )}")
                }
                .forEach{(kategori, antall) ->
                    val gaugeName = "cv.eures.eksport.antall.samtykker.${kategori.lowercase()}"
                    addOrUpdateGauge(gaugeName, antall)
                }

            val (created, modified, closed) = euresService.getAllCounts()
            addOrUpdateGauge("cv.eures.eksport.antall.euresService.created.total", created)
            addOrUpdateGauge("cv.eures.eksport.antall.euresService.modified.total", modified)
            addOrUpdateGauge("cv.eures.eksport.antall.euresService.closed.total", closed)
            log.info("Metric: $created opprettet, $modified endret, $closed slettet")

            val countRaw = cvRepository.fetchCountRawCvs()
            addOrUpdateGauge("cv.eures.eksport.antall.raw.total", countRaw)
            log.info("Metric:$countRaw RawCV-er er lagret i EURES databsen")

            val countEscoCache = janzzCacheRepository.getCacheCount()
            addOrUpdateGauge("cv.eures.eksport.antall.escoCache.total", countEscoCache)
            log.info("Metric: $countEscoCache linjer i ESCO cache")


            val startTime = System.currentTimeMillis()
            extractCountries()
            val spentTime = System.currentTimeMillis() - startTime

            addOrUpdateGauge("cv.eures.eksport.time.spent.in.extract.countries", spentTime)


        } catch (e: Exception) {
            log.error("Error while generating metrics", e)
        }

    }

    private fun extractCountries()
          = samtykkeRepository
            .hentAlleLand()
            .map { json -> objectMapper.readValue<List<String>>(json) }
            .flatten()
            .groupingBy { it }
            .eachCount()
            .also { log.debug("Country counter got $it") }
            .onEach { (code, count) -> addOrUpdateGauge("cv.eures.eksport.antall.samtykker.$code", count) }
}