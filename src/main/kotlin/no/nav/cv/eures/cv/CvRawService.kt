package no.nav.cv.eures.cv

import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.eures.samtykke.SamtykkeService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
class CvRawService(private val cvRepository: CvRepository, private val samtykkeService: SamtykkeService) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(CvRawService::class.java)
    }

    @Transactional
    fun updateRawCvRecord(eksisterende: RawCV, dto: CvEndretInternDto, cvAsJson: String) {
        val erUnderOppfølging = dto.oppfolgingsInformasjon?.erUnderOppfolging ?: false
        val eksisterendeErUnderOppfølging = eksisterende.underOppfoelging

        eksisterende.update(
            sistEndret = ZonedDateTime.now(),
            jsonCv = cvAsJson,
            underOppfoelging = erUnderOppfølging,
            meldingstype = RawCV.Companion.RecordType.UPDATE
        )

        try {
            if (eksisterendeErUnderOppfølging && !erUnderOppfølging) samtykkeService.slettSamtykke(eksisterende.foedselsnummer)
            cvRepository.saveAndFlush(eksisterende)
        } catch (e: Exception) {
            log.error("Fikk exception ${e.message} under oppdatring av cv $this", e)
        }
    }

    @Transactional
    fun createRawCvRecord(dto: CvEndretInternDto, cvAsJson: String) {
        cvRepository.deleteCvByAktorId(dto.aktorId)

        val newRawCv = RawCV.create(
            aktoerId = dto.aktorId,
            foedselsnummer = dto.fodselsnummer!!,
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

    @Transactional
    fun createOrUpdateRawCvRecord(dto: CvEndretInternDto, cvAsJson: String) {
        if (dto.fodselsnummer == null) {
            log.warn("Kafkamelding mangler fødselsnummer - hopper over den (${dto.aktorId}) - Meldingstype: ${dto.meldingstype}.")
            return
        }

        cvRepository.hentCvByFoedselsnummer(dto.fodselsnummer)
            ?.let { updateRawCvRecord(it, dto, cvAsJson) }
            ?: createRawCvRecord(dto, cvAsJson)
    }

    fun deleteCv(aktoerId: String): RawCV? = cvRepository.hentCvByAktoerId(aktoerId)
        ?.update(sistEndret = ZonedDateTime.now(), underOppfoelging = false, meldingstype = RawCV.Companion.RecordType.DELETE)
        ?.let { cvRepository.saveAndFlush(it) }
}
