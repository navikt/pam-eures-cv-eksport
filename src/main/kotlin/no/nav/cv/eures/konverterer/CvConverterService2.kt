package no.nav.cv.eures.konverterer

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.eures.cv.*
import no.nav.cv.eures.model.Candidate
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.math.BigInteger
import java.security.MessageDigest

@Service
class CvConverterService2(
        private val cvXmlRepository: CvXmlRepository,
        private val samtykkeRepository: SamtykkeRepository,
        private val cvRepository: CvRepository
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CvConverterService2::class.java)
        val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
    }

    fun updateExisting(cvXml: CvXml?): CvXml? {
        log.debug("Updating existing ${cvXml?.id}")

        if (cvXml == null) return null

        return convertToXml(cvXml.foedselsnummer)
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

    fun createNew(fodselsnummer: String) {
        val now = ZonedDateTime.now()
        convertToXml(fodselsnummer)
            ?.let { (ref, xml, _) ->
                val checksum = md5(xml)
                log.debug("Create New: Before save of ${xml.length} bytes of xml with checksum $checksum")
                fodselsnummer?.let {
                    cvXmlRepository.save(
                        CvXml.create(
                            reference = ref,
                            foedselsnummer = fodselsnummer,
                            opprettet = now,
                            sistEndret = now,
                            slettet = null,
                            xml = xml,
                            checksum = checksum
                        )
                    )
                }
            }
    }

    fun createOrUpdate(fodselsnummer: String) = cvXmlRepository.fetch(fodselsnummer)
        ?.let { updateExisting(it) }
        ?: createNew(fodselsnummer)

    fun delete(fodselsnummer: String): CvXml? = cvXmlRepository.fetch(fodselsnummer)
            ?.let {
                it.slettet = it.slettet ?: ZonedDateTime.now()
                it.xml = ""
                it.checksum = ""
                samtykkeRepository.slettSamtykke(fodselsnummer)
                return@let cvXmlRepository.save(it)
            }


    fun convertToXml(fodselsnummer: String): Triple<String, String, Candidate>? {
        val record = cvRepository.hentCvByFoedselsnummer(fodselsnummer)
        val dto = objectMapper.readValue<CvEndretInternDto>(record?.jsonCv!!)
        return dto
            .let {
                log.debug("Got CV aktoerid: ${it.aktorId}")

                samtykkeRepository.hentSamtykke(fodselsnummer)
                    ?.run {
                        val (xml, previewJson) = try {
                            val candidate = CandidateConverter2(it, this).toXmlRepresentation()
                            Pair(XmlSerializer.serialize(candidate), candidate)
                        } catch (e: Exception) {
                            log.error("Failed to convert CV to XML for candidate ${it.aktorId}", e)
                            throw CvNotConvertedException("Failed to convert CV to XML for candidate ${it.aktorId}", e)
                        }
                        return@let Triple(it.kandidatNr ?: "", xml, previewJson)
                    }

            }
    }

}

class CvNotConvertedException(msg: String, e: Exception): Exception(msg, e)