package no.nav.cv.eures.scheduled

import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.CvXml
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.cv.RawCV
import no.nav.cv.eures.samtykke.Samtykke
import no.nav.cv.eures.samtykke.SamtykkeRepository
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime

@SpringBootTest
@EnableMockOAuth2Server
internal class PruneSamtykkeTest {

    @Autowired
    lateinit var samtykkeRepository: SamtykkeRepository

    @Autowired
    lateinit var cvRepository: CvRepository

    @Autowired
    lateinit var cvXmlRepository: CvXmlRepository

    @Autowired
    lateinit var pruneSamtykke: PruneSamtykke

    @Test
    fun `delete old samtykke`() {
        val deleteThisFoedselsnummer = "111"
        val deleteThisSistEndret = ZonedDateTime.now().minusYears(2)

        val keepThisFoedselsnummer = "222"
        val keepThisSistEndret = ZonedDateTime.now()

        val deleteThisButKeepRawCv = RawCV.create(
            aktoerId = "aid1",
            foedselsnummer = deleteThisFoedselsnummer,
            sistEndret = deleteThisSistEndret,
            rawAvro = "Raw Avro",
            meldingstype = RawCV.Companion.RecordType.CREATE)

        val deleteThisXmlCv = CvXml.create(
            reference = "delete",
            foedselsnummer = deleteThisFoedselsnummer,
            opprettet = deleteThisSistEndret,
            sistEndret = deleteThisSistEndret,
            slettet = null,
            xml = "xml string",
            checksum = "checksum",
            aktorId = "123")

        val keepThisButKeepRawCv = RawCV.create(
            aktoerId = "aid2",
            foedselsnummer = keepThisFoedselsnummer,
            sistEndret = keepThisSistEndret,
            rawAvro = "Raw Avro",
            meldingstype = RawCV.Companion.RecordType.CREATE)

        val keepThisXmlCv = CvXml.create(
            reference = "keep",
            foedselsnummer = keepThisFoedselsnummer,
            opprettet = keepThisSistEndret,
            sistEndret = keepThisSistEndret,
            slettet = null,
            xml = "xml string",
            checksum = "checksum",
            aktorId="123")


        samtykkeRepository.oppdaterSamtykke(deleteThisFoedselsnummer, Samtykke(deleteThisSistEndret))
        cvRepository.save(deleteThisButKeepRawCv)
        cvXmlRepository.save(deleteThisXmlCv)

        samtykkeRepository.oppdaterSamtykke(keepThisFoedselsnummer, Samtykke(keepThisSistEndret))
        cvRepository.save(keepThisButKeepRawCv)
        cvXmlRepository.save(keepThisXmlCv)


        pruneSamtykke.pruneBasedOnSamtykkeExpiry()

        val deletedSamtykke = samtykkeRepository.hentSamtykke(deleteThisFoedselsnummer)
        val notDeletedRawCv = cvRepository.hentCvByFoedselsnummer(deleteThisFoedselsnummer)
        val deletedXmlCv = cvXmlRepository.fetch(deleteThisFoedselsnummer)

        assertNull(deletedSamtykke)
        assertNotNull(notDeletedRawCv)
        assertNotNull(deletedXmlCv?.slettet)

        val keptSamtykke = samtykkeRepository.hentSamtykke(keepThisFoedselsnummer)
        val keptRawCv = cvRepository.hentCvByFoedselsnummer(keepThisFoedselsnummer)
        val keptXmlCv = cvXmlRepository.fetch(keepThisFoedselsnummer)

        assertNotNull(keptSamtykke)
        assertNotNull(keptRawCv)
        assertNull(keptXmlCv?.slettet)

    }
}