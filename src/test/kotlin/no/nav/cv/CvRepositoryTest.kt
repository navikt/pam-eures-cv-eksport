package no.nav.cv

import io.micronaut.test.annotation.MicronautTest
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.RawCV
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest
class CvRepositoryTest {

    @Inject
    lateinit var cvRepository: CvRepository

    private val testData = CvTestData()

    @Test
    fun `finn cv knyttet til aktorid`() {
        val cv = RawCV.create(testData.aktorId1, testData.now, testData.rawAvro1Base64)

        cvRepository.lagreCv(cv)

        val hentet = cvRepository.hentCv(testData.aktorId1)

        assertNotNull(hentet)
        assertEquals(hentet?.aktorId, testData.aktorId1)
        assertEquals(hentet?.sistEndret, testData.now)
        assertEquals(hentet?.rawAvro, testData.rawAvro1Base64)
    }

    @Test
    fun `finn en av flere cv`() {
        val cv1 = RawCV.create(testData.aktorId1, testData.now, testData.rawAvro1Base64)
        val cv2 = RawCV.create(testData.aktorId2, testData.yesterday, testData.rawAvro2Base64)

        cvRepository.lagreCv(cv1)
        cvRepository.lagreCv(cv2)

        val hentet1 = cvRepository.hentCv(testData.aktorId1)

        assertNotNull(hentet1)
        assertEquals(hentet1?.aktorId, testData.aktorId1)
        assertEquals(hentet1?.sistEndret, testData.now)
        assertEquals(hentet1?.rawAvro, testData.rawAvro1Base64)

        val hentet2 = cvRepository.hentCv(testData.aktorId2)

        assertNotNull(hentet2)
        assertEquals(hentet2?.aktorId, testData.aktorId2)
        assertEquals(hentet2?.sistEndret, testData.yesterday)
        assertEquals(hentet2?.rawAvro, testData.rawAvro2Base64)
    }

    @Test
    fun `cv blir oppdatert`() {
        val cv1 = RawCV.create(testData.aktorId1, testData.yesterday, testData.rawAvro1Base64)

        cvRepository.lagreCv(cv1)

        val cv2 = cvRepository.hentCv(testData.aktorId1)
                ?.update(testData.aktorId1, testData.now, testData.rawAvro2Base64)

        assertNotNull(cv2)
        cvRepository.lagreCv(cv2!!)

        val hentet2 = cvRepository.hentCv(testData.aktorId1)

        assertEquals(hentet2?.aktorId, testData.aktorId1)
        assertEquals(hentet2?.sistEndret, testData.now)
        assertEquals(hentet2?.rawAvro, testData.rawAvro2Base64)
    }

    @Test
    fun `ukjent cv gir null`() {
        val ukjentCv = cvRepository.hentCv(testData.aktorIdUkjent)

        assertNull(ukjentCv)
    }
}