package no.nav.cv.eures.samtykke

import no.nav.cv.eures.konverterer.Konverterer
import javax.inject.Singleton

@Singleton
class SamtykkeService(
        private val samtykkeRepository: SamtykkeRepository,
        private val konverterer: Konverterer
) {
    fun hentSamtykke(aktoerId: String): Samtykke? = samtykkeRepository.hentSamtykke(aktoerId)
    fun slettSamtykke(aktoerId: String): Int {
        konverterer.slett(aktoerId)
        return samtykkeRepository.slettSamtykke(aktoerId)
    }

    fun oppdaterSamtykke(samtykke: Samtykke) = samtykkeRepository.oppdaterSamtykke(samtykke)
            .run { konverterer.oppdater(samtykke.aktoerId) }
}
