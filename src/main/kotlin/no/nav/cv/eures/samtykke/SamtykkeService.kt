package no.nav.cv.eures.samtykke

import io.micrometer.core.instrument.MeterRegistry
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.konverterer.CvConverterService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SamtykkeService(
        private val samtykkeRepository: SamtykkeRepository,
        private val cvConverterService: CvConverterService,
        private val cvRepository: CvRepository,
        private val meterRegistry: MeterRegistry
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(SamtykkeService::class.java)
    }

    fun hentSamtykke(foedselsnummer: String): Samtykke? =
            samtykkeRepository.hentSamtykke(foedselsnummer)

    fun slettSamtykke(foedselsnummer: String) {
        val existing = samtykkeRepository.hentSamtykke(foedselsnummer)

        log.info("Sletter samtykke for ${foedselsnummer.take(1)}.........${foedselsnummer.takeLast(1)}")

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

        log.info("Oppdaterer samtykke for ${foedselsnummer.take(1)}.........${foedselsnummer.takeLast(1)} : $samtykke")

        try {
            if(existing == null)
                meterRegistry.counter("cv.eures.eksport.samtykke.opprettet").increment(1.0)
            else
                meterRegistry.counter("cv.eures.eksport.samtykke.oppdatert").increment(1.0)
        } catch (e: Exception) {
            log.warn("Got exception when trying to increase metric counter for created samtykke", e)
        }

        samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykke)
                .run { cvConverterService.createOrUpdate(foedselsnummer) }

        try {
            log.info("Merker CV for oppdatering ${foedselsnummer.take(1)}.........${foedselsnummer.takeLast(1)} etter oppdatert samtykke")

            cvRepository.hentCvByFoedselsnummer(foedselsnummer)
                ?.let { rawCV ->
                    rawCV.prosessert = false
                    cvRepository.save(rawCV)
                }
                ?: kotlin.run {
                    log.warn("Fant ingen eksisterende RAW CV for ${foedselsnummer.take(1)}.........${foedselsnummer.takeLast(1)}")
                }

        } catch (e: Exception) {
            log.error("Got error when reprocessing CV after changed Samtykke ${e.message}", e)
        }

    }
}
