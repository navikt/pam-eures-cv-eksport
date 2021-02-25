package no.nav.cv.eures.samtykke

import io.micrometer.core.instrument.MeterRegistry
import no.nav.cv.eures.konverterer.CvConverterService
import org.springframework.stereotype.Service

@Service
class SamtykkeService(
        private val samtykkeRepository: SamtykkeRepository,
        private val cvConverterService: CvConverterService,
        private val meterRegistry: MeterRegistry
) {
    fun hentSamtykke(foedselsnummer: String): Samtykke? =
            samtykkeRepository.hentSamtykke(foedselsnummer)

    fun slettSamtykke(foedselsnummer: String) {
        meterRegistry.counter("cv.eures.eksport.samtykke.slettet").increment(1.0)

        cvConverterService.delete(foedselsnummer)
        samtykkeRepository.slettSamtykke(foedselsnummer)
    }

    fun oppdaterSamtykke(foedselsnummer: String, samtykke: Samtykke) {
        val existing = samtykkeRepository.hentSamtykke(foedselsnummer)

        if(existing == null)
            meterRegistry.counter("cv.eures.eksport.samtykke.opprettet").increment(1.0)

        samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykke)
                .run { cvConverterService.createOrUpdate(foedselsnummer) }
    }
}
