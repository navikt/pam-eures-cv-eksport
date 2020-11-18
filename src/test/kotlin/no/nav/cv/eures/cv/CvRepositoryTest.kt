package no.nav.cv.eures.cv

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CvRepositoryTest {

    @Autowired
    lateinit var cvRepository: CvRepository

    private val testData = CvTestData()

    @Test
    fun `finn cv knyttet til foedselsnummer`() {
        val cv = RawCV.create(testData.aktoerId1, testData.foedselsnummer1,
                testData.now, testData.rawAvro1Base64, false, RawCV.Companion.RecordType.CREATE)

        cvRepository.lagreCv(cv)

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

        cvRepository.lagreCv(cv1)
        cvRepository.lagreCv(cv2)

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

        cvRepository.lagreCv(cv1)

        val cv2 = cvRepository.hentCvByFoedselsnummer(testData.foedselsnummer1)
                ?.update(testData.aktoerId1, testData.foedselsnummer1, testData.now, testData.rawAvro2Base64,
                    testData.underOppfoelging, RawCV.Companion.RecordType.UPDATE)

        assertNotNull(cv2)
        cvRepository.lagreCv(cv2!!)

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
}