package no.nav.cv.eures.scheduled

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.cv.RawCV
import no.nav.cv.eures.konverterer.CvConverterService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Requires(notEnv = ["test"])
@Singleton
class MessageProcessor(
        private val cvRepository: CvRepository,
        private val cvXmlRepository: CvXmlRepository,
        private val cvConverterService: CvConverterService
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(MessageProcessor::class.java)
    }

    private fun List<RawCV>.processRecords(): List<RawCV> =
            partition { rawCv -> rawCv.meldingstype == Meldingstype.SLETT }
                .let {
                    val (deleted, createdOrModified) = it
                    listOf(
                            deleted.let { rawCvs ->
                                rawCvs.forEach { rawCv -> cvConverterService.delete(rawCv.foedselsnummer) }
                                rawCvs
                            },
                            createdOrModified.let { rawCvs ->
                                // TODO - Should update existing ones AND create non-existing based on
                                //  whether samtykke exists or not
                                cvXmlRepository.fetchAllActiveCvsByFoedselsnummer(rawCvs.map(RawCV::foedselsnummer))
                                        .forEach { cvXml -> cvConverterService.updateExisting(cvXml) }
                                rawCvs
                            }
                    ).flatten()
                }


    @Scheduled(fixedDelay = "5s")
    fun process() = cvRepository.hentUprosesserteCver()
            .processRecords()
            .also { rawCvs ->
                rawCvs.map { rawCv ->  cvRepository.lagreCv(rawCv.also { it.prosessert = true }) }
                if (rawCvs.isNotEmpty()) log.info("Prosesserte ${rawCvs.size} endringer")
            }
}

