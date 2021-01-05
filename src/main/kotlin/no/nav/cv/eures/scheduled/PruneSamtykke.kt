package no.nav.cv.eures.scheduled

import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Profile("!test")
@Service
class PruneSamtykke(
        private val cvRepository: CvRepository,
        private val cvXmlRepository: CvXmlRepository,
        private val samtykkeRepository: SamtykkeRepository
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(PruneSamtykke::class.java)
    }

    // Once per hour (ms) 30 s for testing
    @Scheduled(fixedDelay = 1000 * 30)
    fun prune() = cvRepository.hentGamleCver(ZonedDateTime.now().minusYears(1))
            .onEach { rawCv ->
                log.debug("Sletter cv og samtykke for gammel xml cv ${rawCv.aktoerId}")
                samtykkeRepository.slettSamtykke(rawCv.foedselsnummer)

                cvXmlRepository.fetch(rawCv.foedselsnummer)
                        ?.let { xmlCv ->
                            xmlCv.slettet = ZonedDateTime.now()
                            cvXmlRepository.save(xmlCv)
                        }
            }
            .also { log.info("Slettet ${it.size} gamle samtykker") }
}