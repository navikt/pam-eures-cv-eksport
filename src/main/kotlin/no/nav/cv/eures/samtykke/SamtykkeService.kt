package no.nav.cv.eures.samtykke

import no.nav.cv.eures.konverterer.Konverterer
import javax.inject.Singleton

@Singleton
class SamtykkeService(
        private val samtykkeRepository: SamtykkeRepository,
        private val konverterer: Konverterer
) {
    fun hentSamtykke(foedselsnummer: String): Samtykke? =
            samtykkeRepository.hentSamtykke(foedselsnummer)

    fun slettSamtykke(foedselsnummer: String): Int {
        konverterer.slett(foedselsnummer)
        return samtykkeRepository.slettSamtykke(foedselsnummer)
    }

    fun oppdaterSamtykke(samtykke: Samtykke) = samtykkeRepository.oppdaterSamtykke(samtykke)
            .run { konverterer.oppdaterEllerLag(samtykke.foedselsnummer) }
}
