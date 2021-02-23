package no.nav.cv.eures.samtykke

import io.micrometer.influx.InfluxMeterRegistry
import no.nav.cv.eures.konverterer.CvConverterService
import org.springframework.stereotype.Service

@Service
class SamtykkeService(
        private val samtykkeRepository: SamtykkeRepository,
        private val cvConverterService: CvConverterService,
        private val influxMeterRegistry: InfluxMeterRegistry
) {
    fun hentSamtykke(foedselsnummer: String): Samtykke? =
            samtykkeRepository.hentSamtykke(foedselsnummer)

    fun slettSamtykke(foedselsnummer: String) {
        influxMeterRegistry.counter("cv.eures.eksport.samtykke.slettet").increment(1.0)

        cvConverterService.delete(foedselsnummer)
        samtykkeRepository.slettSamtykke(foedselsnummer)
    }

    fun oppdaterSamtykke(foedselsnummer: String, samtykke: Samtykke) {
        val existing = samtykkeRepository.hentSamtykke(foedselsnummer)

        if(existing == null)
            influxMeterRegistry.counter("cv.eures.eksport.samtykke.opprettet").increment(1.0)

        samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykke)
                .run { cvConverterService.createOrUpdate(foedselsnummer) }
    }
}
