package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Jobbprofil
import no.nav.arbeid.cv.avro.Melding
import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.cv.eures.cv.*
import no.nav.cv.eures.samtykke.SamtykkeRepository
import no.nav.cv.eures.samtykke.SamtykkeService
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.math.BigInteger
import java.security.MessageDigest

@Service
class CvConverterService(
        private val cvRepository: CvRepository,
        private val cvXmlRepository: CvXmlRepository,
        private val samtykkeRepository: SamtykkeRepository
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CvConverterService::class.java)
    }


    // This is definitely not the best solution, but unfortunately
    // it's the only one I see for handling both avro versions without
    // ending up in situations where we re-index things and get errors
    // until we finish processing the first part of the topic.
    private fun RawCV.toMelding(): Melding? {
        val wireBytes = getWireBytes()

        if (wireBytes.isEmpty()) return null

        return try {
            wireBytes.readDatum()
        } catch (e: Exception) {
            wireBytes.readDatum(5)
        } catch (e: Exception) {
            CvConsumer.log.error("Klarte ikke decode kafka melding. Size: ${wireBytes.size}", e)
            throw(e)
        }
    }

    private fun ByteArray.readDatum(avroPrefixByteSize: Int = 7): Melding {
        try {
            // NOTE: The newest AVRO version prefixes 6 bytes instead of 4
            // TODO - Figure out if there's away to avoid this.
            val businessPartOfMessage = slice(avroPrefixByteSize until size).toByteArray()

            val datumReader = SpecificDatumReader(Melding::class.java)
            val decoder = DecoderFactory.get().binaryDecoder(businessPartOfMessage, null)
            return datumReader.read(null, decoder)
        } catch (e: Exception) {
            CvConsumer.log.warn("Klarte ikke å deserialisere avromeldingen med versjon prefiks på: $avroPrefixByteSize bytes", e)
            throw e
        }
    }


    private fun Melding.cvAndProfile(): Pair<Cv?, Jobbprofil?>? = when (meldingstype) {
        Meldingstype.OPPRETT -> Pair(opprettCv?.cv, opprettJobbprofil?.jobbprofil)
        Meldingstype.ENDRE -> Pair(endreCv?.cv, endreJobbprofil?.jobbprofil)
        else -> null
    }

    fun updateExisting(cvXml: CvXml?): CvXml? {
        log.debug("Updating existing ${cvXml?.id}")

        if (cvXml == null) return null

        return convertToXml(cvXml.foedselsnummer)
                ?.let { (_, xml) -> updateIfChanged(cvXml, xml)}
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

    fun createNew(foedselsnummer: String) {
        val now = ZonedDateTime.now()
        convertToXml(foedselsnummer)
                ?.let { (ref, xml) ->
                    val checksum = md5(xml)
                    log.debug("Create New: Before save of ${xml.length} bytes of xml with checksum $checksum")
                    cvXmlRepository.save(CvXml.create(
                            reference = ref,
                            foedselsnummer = foedselsnummer,
                            opprettet = now,
                            sistEndret = now,
                            slettet = null,
                            xml = xml,
                            checksum = checksum
                    ))
                }
    }

    fun createOrUpdate(foedselsnummer: String) = cvXmlRepository.fetch(foedselsnummer)
            ?.let { updateExisting(it) }
            ?: createNew(foedselsnummer)


    fun delete(foedselsnummer: String): CvXml? = cvXmlRepository.fetch(foedselsnummer)
            ?.let {
                it.slettet = it.slettet ?: ZonedDateTime.now()
                it.xml = ""
                samtykkeRepository.slettSamtykke(foedselsnummer)
                return@let cvXmlRepository.save(it)
            }


    fun convertToXml(foedselsnummer: String): Pair<String, String>? {
        val record = cvRepository.hentCvByFoedselsnummer(foedselsnummer)
            ?: run {
                log.info("Trying to convert XML for ${foedselsnummer.take(1)}.........${foedselsnummer.takeLast(1)} but got nothing from raw cv repo ")
                return null
            }
        return record.toMelding()
                ?.cvAndProfile()
                ?.let { (cv, profile) ->

                    log.debug("Got CV aktoerid: ${cv?.aktoerId} Profile ID: ${profile?.jobbprofilId}")

                    cv ?: return@let null

                    samtykkeRepository.hentSamtykke(foedselsnummer)
                            ?.run {
                                val xml = try {
                                    val candidate = CandidateConverter(cv, profile, this).toXmlRepresentation()
                                    XmlSerializer.serialize(candidate)
                                } catch (e: Exception) {
                                    log.error("Failed to convert CV to XML for candidate ${cv.aktoerId}", e)
                                    throw CvNotConvertedException("Failed to convert CV to XML for candidate ${cv.aktoerId}", e)
                                }

                                return@let Pair(cv.arenaKandidatnr, xml)
                            }
                }
            ?: run {
                log.info("Trying to convert XML for ${foedselsnummer.take(1)}.........${foedselsnummer.takeLast(1)} but got null from melding.cvAndProfile() ")
                null
            }


    }

}

class CvNotConvertedException(msg: String, e: Exception): Exception(msg, e)