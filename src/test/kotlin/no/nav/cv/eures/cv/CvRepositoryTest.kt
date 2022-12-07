package no.nav.cv.eures.cv

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@DataJpaTest(includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [Repository::class])])
@EnableMockOAuth2Server
class CvRepositoryTest {

    @Autowired
    lateinit var cvRepository: CvRepository

    @Autowired
    lateinit var cvXmlRepository: CvXmlRepository

    private val testData = CvTestData()

    @Test
    fun `finn cv knyttet til foedselsnummer`() {
        val cv = RawCV.create(testData.aktoerId1, testData.foedselsnummer1,
                testData.now, false, RawCV.Companion.RecordType.CREATE, testData.melding1Serialized)

        cvRepository.saveAndFlush(cv)

        val hentet = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummer1)

        assertNotNull(hentet)
        assertEquals(hentet?.foedselsnummer, testData.foedselsnummer1)
        assertEquals(hentet?.sistEndret, testData.now)
        assertEquals(hentet?.jsonCv, testData.melding1Serialized)
    }

    @Test
    fun `finn en av flere cv`() {
        val cv1 = RawCV.create(testData.aktoerId1, testData.foedselsnummer1,
                testData.now, false, RawCV.Companion.RecordType.CREATE, testData.melding1Serialized)
        val cv2 = RawCV.create(testData.aktoerId2, testData.foedselsnummer2,
                testData.yesterday, false, RawCV.Companion.RecordType.CREATE, testData.melding2Serialized)

        cvRepository.saveAndFlush(cv1)
        cvRepository.saveAndFlush(cv2)

        val hentet1 = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummer1)

        assertNotNull(hentet1)
        assertEquals(hentet1?.foedselsnummer, testData.foedselsnummer1)
        assertEquals(hentet1?.sistEndret, testData.now)
        assertEquals(hentet1?.jsonCv, testData.melding1Serialized)

        val hentet2 = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummer2)

        assertNotNull(hentet2)
        assertEquals(hentet2?.foedselsnummer, testData.foedselsnummer2)
        assertEquals(hentet2?.sistEndret, testData.yesterday)
        assertEquals(hentet2?.jsonCv, testData.melding2Serialized)
    }

    @Test
    fun `cv blir oppdatert`() {
        val cv1 = RawCV.create(testData.aktoerId1, testData.foedselsnummer1,
                testData.now,  false, RawCV.Companion.RecordType.CREATE, testData.melding1Serialized)

        cvRepository.saveAndFlush(cv1)

        val cv2 = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummer1)
                ?.update(testData.aktoerId1, testData.foedselsnummer1, testData.now,
                    testData.underOppfoelging, RawCV.Companion.RecordType.UPDATE, testData.melding2Serialized)

        assertNotNull(cv2)
        cvRepository.saveAndFlush(cv2!!)

        val hentet2 = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummer1)

        assertEquals(hentet2?.foedselsnummer, testData.foedselsnummer1)
        assertEquals(hentet2?.sistEndret, testData.now)
        assertEquals(hentet2?.jsonCv, testData.melding2Serialized)
    }

    @Test
    fun `ukjent cv gir null`() {
        val ukjentCv = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummerUkjent)

        assertNull(ukjentCv)
    }

    @Test
    fun `gammel cv blir hentet`() {
        val nyCv = RawCV.create(testData.aktoerId1, testData.foedselsnummer1, testData.now,
                false, RawCV.Companion.RecordType.CREATE,testData.melding1Serialized)

        val nyXmlCv = CvXml.create("", testData.aktoerId1, testData.now, testData.now,
                null, "", "")

        val gammelCv = RawCV.create(testData.aktoerId2, testData.foedselsnummer2, testData.yesterday,
                 false, RawCV.Companion.RecordType.CREATE, testData.melding2Serialized)

        val gammelXmlCv = CvXml.create("", testData.aktoerId2, testData.yesterday, testData.yesterday,
                null, "", "")

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
        val cv = RawCV.create(testData.aktoerId1, testData.foedselsnummer1, testData.yesterday, false,
                RawCV.Companion.RecordType.CREATE, testData.melding1Serialized)

        val xmlCv = CvXml.create("", testData.aktoerId1, testData.yesterday, testData.yesterday,
                ZonedDateTime.now().minusHours(1), "", "")

        xmlCv.foedselsnummer = testData.foedselsnummer1

        cvRepository.saveAndFlush(cv)
        cvXmlRepository.saveAndFlush(xmlCv)

        val result = cvRepository.hentGamleCver(ZonedDateTime.now())

        assertEquals(0, result.size)
    }
}