package no.nav.cv.eures.cv

import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.eures.samtykke.SamtykkeService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class CvRawService(
    private val cvRepository: CvRepository,
    private val samtykkeService: SamtykkeService
    ) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CvRawService::class.java)
    }

    fun createOrUpdateRawCvRecord(dto: CvEndretInternDto, cvAsJson: String) {
        val foedselsnummer = dto.fodselsnummer
        val aktoerId = dto.aktorId

        if (foedselsnummer == null) {
            log.warn("Kafkamelding mangler fødselsnummer - hopper over den (${aktoerId}) - Meldingstype: ${dto.meldingstype}.")
            return
        }

        val existing = cvRepository.hentCvByFoedselsnummer(foedselsnummer)

        if(existing != null) {
            if (existing.underOppfoelging && (dto.oppfolgingsInformasjon != null && !dto.oppfolgingsInformasjon.erUnderOppfolging)) {
                log.debug("Deleting ${existing.aktoerId} due to not being 'under oppfølging' anymore")

                // By deleting samtykke we also mark the XML CV for deletion
                try {
                    samtykkeService.slettSamtykke(foedselsnummer)
                } catch (e: Exception) {
                    log.error("Fikk exception ${e.message} under sletting av cv $this", e)
                }
            } else {
                log.debug("Updating ${existing.aktoerId}")
                existing.update(
                    sistEndret = ZonedDateTime.now(),
                    jsonCv = cvAsJson,
                    underOppfoelging = (dto.oppfolgingsInformasjon != null),
                    meldingstype = RawCV.Companion.RecordType.UPDATE
                )

                try {
                    cvRepository.saveAndFlush(existing)
                } catch (e: Exception) {
                    log.error("Fikk exception ${e.message} under oppdatring av cv $this", e)
                }
            }
        } else {
            log.debug("inserting new record for $aktoerId")

            cvRepository.deleteCvByAktorId(aktoerId)

            val newRawCv = RawCV.create(
                aktoerId = aktoerId,
                foedselsnummer = foedselsnummer,
                sistEndret = ZonedDateTime.now(),
                jsonCv = cvAsJson,
                underOppfoelging = dto.oppfolgingsInformasjon?.erUnderOppfolging,
                meldingstype = RawCV.Companion.RecordType.CREATE
            )

            try {
                cvRepository.saveAndFlush(newRawCv)
            } catch (e: Exception) {
                log.error("Fikk exception ${e.message} under lagring av cv $this", e)
            }
        }
    }
    fun deleteCv(aktoerId: String): RawCV? = cvRepository.hentCvByAktoerId(aktoerId)?.update(
        sistEndret = ZonedDateTime.now(),
        underOppfoelging = false,
        meldingstype = RawCV.Companion.RecordType.DELETE
    )?.let { cvRepository.saveAndFlush(it) }
}