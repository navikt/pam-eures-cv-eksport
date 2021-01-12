package no.nav.cv.eures.samtykke

import no.nav.cv.eures.cv.CvXml
import no.nav.cv.eures.konverterer.CvConverterService
import org.springframework.stereotype.Service

@Service
class SamtykkeService(
        private val samtykkeRepository: SamtykkeRepository,
        private val cvConverterService: CvConverterService
) {
    fun hentSamtykke(foedselsnummer: String): Samtykke? =
            samtykkeRepository.hentSamtykke(foedselsnummer)

    fun slettSamtykke(foedselsnummer: String) {
        cvConverterService.delete(foedselsnummer)
        samtykkeRepository.slettSamtykke(foedselsnummer)
    }

    fun oppdaterSamtykke(foedselsnummer: String, samtykke: Samtykke) =
            samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykke)
                    .run { cvConverterService.createOrUpdate(foedselsnummer) }
}
