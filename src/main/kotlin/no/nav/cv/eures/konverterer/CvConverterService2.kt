package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Jobbprofil
import no.nav.arbeid.cv.avro.Melding
import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.eures.cv.*
import no.nav.cv.eures.model.Candidate
import no.nav.cv.eures.samtykke.SamtykkeRepository
import no.nav.cv.eures.samtykke.SamtykkeService
import no.nav.cv.eures.util.toMelding
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.math.BigInteger
import java.security.MessageDigest

@Service
class CvConverterService2(
        private val cvXmlRepository: CvXmlRepository,
        private val samtykkeRepository: SamtykkeRepository
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CvConverterService::class.java)
    }

    fun updateExisting(cvXml: CvXml?, dto: CvEndretInternDto): CvXml? {
        CvConverterService.log.debug("Updating existing ${cvXml?.id}")

        if (cvXml == null) return null

        return convertToXml(dto)
            ?.let { (_, xml, _) -> updateIfChanged(cvXml, xml)}
    }

    fun updateIfChanged(cvXml: CvXml, newXml: String): CvXml {
        val now = ZonedDateTime.now()

        val newChecksum = md5(newXml)

        if(cvXml.checksum == newChecksum) {
            log.debug("${cvXml.id} not changed, not saving")
            return cvXml
        }

        log.debug("Update Existing: Saving ${newXml.length} bytes of xml with checksum $newChecksum")
        cvXml.sistEndret = now
        cvXml.slettet = null
        cvXml.xml = newXml
        cvXml.checksum = newChecksum
        return cvXmlRepository.save(cvXml)

    }

    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun createNew(dto: CvEndretInternDto) {
        val now = ZonedDateTime.now()
        convertToXml(dto)
            ?.let { (ref, xml, _) ->
                val checksum = md5(xml)
                CvConverterService.log.debug("Create New: Before save of ${xml.length} bytes of xml with checksum $checksum")
                cvXmlRepository.save(CvXml.create(
                    reference = ref,
                    foedselsnummer = dto.foedselsnummer.orEmpty(),
                    opprettet = now,
                    sistEndret = now,
                    slettet = null,
                    xml = xml,
                    checksum = checksum
                ))
            }
    }


    fun createOrUpdate(dto: CvEndretInternDto) = cvXmlRepository.fetch(dto.foedselsnummer!!)
        ?.let { updateExisting(it, dto) }
        ?: createNew(dto)

    fun delete(foedselsnummer: String): CvXml? = cvXmlRepository.fetch(foedselsnummer)
            ?.let {
                it.slettet = it.slettet ?: ZonedDateTime.now()
                it.xml = ""
                it.checksum = ""
                samtykkeRepository.slettSamtykke(foedselsnummer)
                return@let cvXmlRepository.save(it)
            }


    fun convertToXml(dto: CvEndretInternDto): Triple<String, String, Candidate>? {
        return dto
            ?.let {
                CvConverterService.log.debug("Got CV aktoerid: ${it.aktorId}")

                samtykkeRepository.hentSamtykke(it.foedselsnummer!!)
                    ?.run {
                        val (xml, previewJson) = try {
                            val candidate = CandidateConverter2(it, this).toXmlRepresentation() //TODO nullet ut profil her OBS OBS
                            Pair(XmlSerializer.serialize(candidate), candidate)
                        } catch (e: Exception) {
                            CvConverterService.log.error("Failed to convert CV to XML for candidate ${it.aktorId}", e)
                            throw CvNotConvertedException("Failed to convert CV to XML for candidate ${it.aktorId}", e)
                        }
                        return@let Triple(it.kandidatNr ?: "", xml, previewJson)
                    }

            }
    }

}
