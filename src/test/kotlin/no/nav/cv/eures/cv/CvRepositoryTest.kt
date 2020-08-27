package no.nav.cv.eures.cv

import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class CvRepositoryTest {

    @Inject
    lateinit var cvRepository: CvRepository

    private val testData = CvTestData()

    @Test
    fun `finn cv knyttet til aktorid`() {
        val cv = RawCV.create(testData.aktoerId1, testData.now, testData.rawAvro1Base64)

        cvRepository.lagreCv(cv)

        val hentet = cvRepository.hentCv(testData.aktoerId1)

        assertNotNull(hentet)
        assertEquals(hentet?.aktoerId, testData.aktoerId1)
        assertEquals(hentet?.sistEndret, testData.now)
        assertEquals(hentet?.rawAvro, testData.rawAvro1Base64)
    }

    @Test
    fun `finn en av flere cv`() {
        val cv1 = RawCV.create(testData.aktoerId1, testData.now, testData.rawAvro1Base64)
        val cv2 = RawCV.create(testData.aktoerId2, testData.yesterday, testData.rawAvro2Base64)

        cvRepository.lagreCv(cv1)
        cvRepository.lagreCv(cv2)

        val hentet1 = cvRepository.hentCv(testData.aktoerId1)

        assertNotNull(hentet1)
        assertEquals(hentet1?.aktoerId, testData.aktoerId1)
        assertEquals(hentet1?.sistEndret, testData.now)
        assertEquals(hentet1?.rawAvro, testData.rawAvro1Base64)

        val hentet2 = cvRepository.hentCv(testData.aktoerId2)

        assertNotNull(hentet2)
        assertEquals(hentet2?.aktoerId, testData.aktoerId2)
        assertEquals(hentet2?.sistEndret, testData.yesterday)
        assertEquals(hentet2?.rawAvro, testData.rawAvro2Base64)
    }

    @Test
    fun `cv blir oppdatert`() {
        val cv1 = RawCV.create(testData.aktoerId1, testData.yesterday, testData.rawAvro1Base64)

        cvRepository.lagreCv(cv1)

        val cv2 = cvRepository.hentCv(testData.aktoerId1)
                ?.update(testData.aktoerId1, testData.now, testData.rawAvro2Base64)

        assertNotNull(cv2)
        cvRepository.lagreCv(cv2!!)

        val hentet2 = cvRepository.hentCv(testData.aktoerId1)

        assertEquals(hentet2?.aktoerId, testData.aktoerId1)
        assertEquals(hentet2?.sistEndret, testData.now)
        assertEquals(hentet2?.rawAvro, testData.rawAvro2Base64)
    }

    @Test
    fun `ukjent cv gir null`() {
        val ukjentCv = cvRepository.hentCv(testData.aktoerIdUkjent)

        assertNull(ukjentCv)
    }
}