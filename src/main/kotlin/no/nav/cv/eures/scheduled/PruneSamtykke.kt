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

@Service
class PruneSamtykke(
        private val cvRepository: CvRepository,
        private val cvXmlRepository: CvXmlRepository,
        private val samtykkeRepository: SamtykkeRepository
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(PruneSamtykke::class.java)
    }

    // Once a day (ms)
    @Profile("!test")
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24 * 1L)
    fun prune() {
        pruneBasedOnSamtykkeExpiry()
        pruneBasedOnCvExpiry()
    }

    fun pruneBasedOnSamtykkeExpiry() {
        samtykkeRepository.hentGamleSamtykker(ZonedDateTime.now().minusYears(1))
            .onEach {
                // To get a loggable ID we need to look up aktoerId from the RawCV
                val rawCv = cvRepository.hentCvByFoedselsnummer(it.foedselsnummer)

                log.debug("Sletter samtykke og xml CV for ${rawCv?.aktoerId ?: "bruker uten aktørid"}")
                samtykkeRepository.slettSamtykke(it.foedselsnummer)

                cvXmlRepository.fetch(it.foedselsnummer)
                    ?.let { xmlCv ->
                        xmlCv.slettet = ZonedDateTime.now()
                        xmlCv.xml = ""
                        xmlCv.checksum = ""
                        cvXmlRepository.save(xmlCv)
                    }
            }
            .also { log.info("Slettet ${it.size} samtykker og xml CVer fordi det er mer enn ett år siden sist samtykke") }
    }

    fun pruneBasedOnCvExpiry() {

        // CVs that hasn't been updated in a year. We might remove this later
        cvRepository.hentGamleCver(ZonedDateTime.now().minusYears(1))
            .onEach { rawCv ->
                log.debug("Sletter cv og samtykke for gammel xml cv ${rawCv.aktoerId}")
                samtykkeRepository.slettSamtykke(rawCv.foedselsnummer)

                cvXmlRepository.fetch(rawCv.foedselsnummer)
                    ?.let { xmlCv ->
                        xmlCv.slettet = ZonedDateTime.now()
                        xmlCv.xml = ""
                        cvXmlRepository.save(xmlCv)
                    }
            }
            .also { log.info("Slettet ${it.size} gamle samtykker") }
    }
}