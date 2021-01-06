package no.nav.cv.eures.scheduled

import no.nav.cv.eures.konverterer.CvConverterService
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Profile("!test")
@Service
class XmlUpdater (
        private val cvConverterService: CvConverterService,
        private val samtykkeRepository: SamtykkeRepository
){

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24)
    fun updateXmlCv() {
        val foedselsnumre = samtykkeRepository.finnFoedselsnumre()

        foedselsnumre.forEach { cvConverterService.createOrUpdate(it) }
    }
}