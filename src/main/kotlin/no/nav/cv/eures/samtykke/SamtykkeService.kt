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

    fun slettSamtykke(foedselsnummer: String) =
            cvConverterService.delete(foedselsnummer)

    fun oppdaterSamtykke(foedselsnummer: String, samtykke: Samtykke) =
            samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykke)
                    .run { cvConverterService.createOrUpdate(foedselsnummer) }
}
