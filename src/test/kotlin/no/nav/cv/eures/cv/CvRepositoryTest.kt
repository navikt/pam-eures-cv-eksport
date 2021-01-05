package no.nav.cv.eures.cv

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Repository
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.ZonedDateTime

@DataJpaTest(includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [Repository::class])])
@Import(TokenGeneratorConfiguration::class)
class CvRepositoryTest {

    @Autowired
    lateinit var cvRepository: CvRepository

    @Autowired
    lateinit var cvXmlRepository: CvXmlRepository

    private val testData = CvTestData()

    @Test
    fun `finn cv knyttet til foedselsnummer`() {
        val cv = RawCV.create(testData.aktoerId1, testData.foedselsnummer1,
                testData.now, testData.rawAvro1Base64, false, RawCV.Companion.RecordType.CREATE)

        cvRepository.saveAndFlush(cv)

        val hentet = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummer1)

        assertNotNull(hentet)
        assertEquals(hentet?.foedselsnummer, testData.foedselsnummer1)
        assertEquals(hentet?.sistEndret, testData.now)
        assertEquals(hentet?.rawAvro, testData.rawAvro1Base64)
    }

    @Test
    fun `finn en av flere cv`() {
        val cv1 = RawCV.create(testData.aktoerId1, testData.foedselsnummer1,
                testData.now, testData.rawAvro1Base64, false, RawCV.Companion.RecordType.CREATE)
        val cv2 = RawCV.create(testData.aktoerId2, testData.foedselsnummer2,
                testData.yesterday, testData.rawAvro2Base64, false, RawCV.Companion.RecordType.CREATE)

        cvRepository.saveAndFlush(cv1)
        cvRepository.saveAndFlush(cv2)

        val hentet1 = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummer1)

        assertNotNull(hentet1)
        assertEquals(hentet1?.foedselsnummer, testData.foedselsnummer1)
        assertEquals(hentet1?.sistEndret, testData.now)
        assertEquals(hentet1?.rawAvro, testData.rawAvro1Base64)

        val hentet2 = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummer2)

        assertNotNull(hentet2)
        assertEquals(hentet2?.foedselsnummer, testData.foedselsnummer2)
        assertEquals(hentet2?.sistEndret, testData.yesterday)
        assertEquals(hentet2?.rawAvro, testData.rawAvro2Base64)
    }

    @Test
    fun `cv blir oppdatert`() {
        val cv1 = RawCV.create(testData.aktoerId1, testData.foedselsnummer1,
                testData.now, testData.rawAvro1Base64, false, RawCV.Companion.RecordType.CREATE)

        cvRepository.saveAndFlush(cv1)

        val cv2 = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummer1)
                ?.update(testData.aktoerId1, testData.foedselsnummer1, testData.now, testData.rawAvro2Base64,
                    testData.underOppfoelging, RawCV.Companion.RecordType.UPDATE)

        assertNotNull(cv2)
        cvRepository.saveAndFlush(cv2!!)

        val hentet2 = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummer1)

        assertEquals(hentet2?.foedselsnummer, testData.foedselsnummer1)
        assertEquals(hentet2?.sistEndret, testData.now)
        assertEquals(hentet2?.rawAvro, testData.rawAvro2Base64)
    }

    @Test
    fun `ukjent cv gir null`() {
        val ukjentCv = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummerUkjent)

        assertNull(ukjentCv)
    }

    @Test
    fun `gammel cv blir hentet`() {
        val nyCv = RawCV.create(testData.aktoerId1, testData.foedselsnummer1, testData.now,
                testData.rawAvro1Base64, false, RawCV.Companion.RecordType.CREATE)

        val nyXmlCv = CvXml.create("", testData.aktoerId1, testData.now, testData.now,
                null, "")

        val gammelCv = RawCV.create(testData.aktoerId2, testData.foedselsnummer2, testData.yesterday,
                testData.rawAvro1Base64, false, RawCV.Companion.RecordType.CREATE)

        val gammelXmlCv = CvXml.create("", testData.aktoerId2, testData.yesterday, testData.yesterday,
                null, "")

        nyXmlCv.foedselsnummer = testData.foedselsnummer1
        gammelXmlCv.foedselsnummer = testData.foedselsnummer2

        cvRepository.saveAndFlush(nyCv)
        cvRepository.saveAndFlush(gammelCv)

        cvXmlRepository.saveAndFlush(nyXmlCv)
        cvXmlRepository.saveAndFlush(gammelXmlCv)

        val result = cvRepository.hentGamleCver(ZonedDateTime.now().minusHours(1))

        assertEquals(1, result.size)
    }

    @Test
    fun `gammel cv blir ikke hentet hvis den er markert slettet`() {
        val cv = RawCV.create(testData.aktoerId1, testData.foedselsnummer1, testData.yesterday,
                testData.rawAvro1Base64, false, RawCV.Companion.RecordType.CREATE)

        val xmlCv = CvXml.create("", testData.aktoerId1, testData.yesterday, testData.yesterday,
                ZonedDateTime.now().minusHours(1), "")

        xmlCv.foedselsnummer = testData.foedselsnummer1

        cvRepository.saveAndFlush(cv)
        cvXmlRepository.saveAndFlush(xmlCv)

        val result = cvRepository.hentGamleCver(ZonedDateTime.now())

        assertEquals(0, result.size)
    }
}