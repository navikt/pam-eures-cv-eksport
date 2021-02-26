package no.nav.cv.eures.samtykke

import io.micrometer.core.instrument.MeterRegistry
import no.nav.cv.eures.konverterer.CvConverterService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SamtykkeService(
        private val samtykkeRepository: SamtykkeRepository,
        private val cvConverterService: CvConverterService,
        private val meterRegistry: MeterRegistry
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(SamtykkeService::class.java)
    }

    fun hentSamtykke(foedselsnummer: String): Samtykke? =
            samtykkeRepository.hentSamtykke(foedselsnummer)

    fun slettSamtykke(foedselsnummer: String) {
        val existing = samtykkeRepository.hentSamtykke(foedselsnummer)

        try {
            if(existing != null)
                meterRegistry.counter("cv.eures.eksport.samtykke.slettet").increment(1.0)
        } catch (e: Exception) {
            log.warn("Got exception when trying to increase metric counter for deleted samtykke", e)
        }

        cvConverterService.delete(foedselsnummer)
        samtykkeRepository.slettSamtykke(foedselsnummer)
    }

    fun oppdaterSamtykke(foedselsnummer: String, samtykke: Samtykke) {
        val existing = samtykkeRepository.hentSamtykke(foedselsnummer)

        try {
            if(existing == null)
                meterRegistry.counter("cv.eures.eksport.samtykke.opprettet").increment(1.0)
        } catch (e: Exception) {
            log.warn("Got exception when trying to increase metric counter for created samtykke", e)
        }

        samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykke)
                .run { cvConverterService.createOrUpdate(foedselsnummer) }
    }
}
