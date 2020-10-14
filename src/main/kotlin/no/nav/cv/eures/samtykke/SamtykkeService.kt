package no.nav.cv.eures.samtykke

import no.nav.cv.eures.konverterer.CvConverterService
import javax.inject.Singleton

@Singleton
class SamtykkeService(
        private val samtykkeRepository: SamtykkeRepository,
        private val cvConverterService: CvConverterService
) {
    fun hentSamtykke(foedselsnummer: String): Samtykke? =
            samtykkeRepository.hentSamtykke(foedselsnummer)

    fun slettSamtykke(foedselsnummer: String): Int {
        cvConverterService.delete(foedselsnummer)
        return samtykkeRepository.slettSamtykke(foedselsnummer)
    }

    fun oppdaterSamtykke(samtykke: Samtykke) = samtykkeRepository.oppdaterSamtykke(samtykke)
            .run { cvConverterService.createOrUpdate(samtykke.foedselsnummer) }
}
