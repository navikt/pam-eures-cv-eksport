package no.nav.cv

import io.micronaut.test.annotation.MicronautTest
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.RawCV
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest
class CvRepositoryTest {

    @Inject
    lateinit var cvRepository: CvRepository

    private val now = ZonedDateTime.now()
    private val yesterday = ZonedDateTime.now().minusDays(1)

    private val aktorId1 = "123"
    private val aktorId2 = "321"
    private val aktorIdUkjent = "ukjent"

    private val rawAvro1 = "raw avro string 1"
    private val rawAvro2 = "raw avro string 2"


    @Test
    fun `finn cv knyttet til aktorid`() {
        val cv = RawCV.create(aktorId1, now, rawAvro1)

        cvRepository.lagreCv(cv)

        val hentet = cvRepository.hentCv(aktorId1)

        assertNotNull(hentet)
        assertEquals(hentet?.aktorId, aktorId1)
        assertEquals(hentet?.sistEndret, now)
        assertEquals(hentet?.rawAvro, rawAvro1)
    }

    @Test
    fun `finn en av flere cv`() {
        val cv1 = RawCV.create(aktorId1, now, rawAvro1)
        val cv2 = RawCV.create(aktorId2, yesterday, rawAvro2)

        cvRepository.lagreCv(cv1)
        cvRepository.lagreCv(cv2)

        val hentet1 = cvRepository.hentCv(aktorId1)

        assertNotNull(hentet1)
        assertEquals(hentet1?.aktorId, aktorId1)
        assertEquals(hentet1?.sistEndret, now)
        assertEquals(hentet1?.rawAvro, rawAvro1)

        val hentet2 = cvRepository.hentCv(aktorId2)

        assertNotNull(hentet2)
        assertEquals(hentet2?.aktorId, aktorId2)
        assertEquals(hentet2?.sistEndret, yesterday)
        assertEquals(hentet2?.rawAvro, rawAvro2)
    }

    @Test
    fun `cv blir oppdatert`() {
        val cv1 = RawCV.create(aktorId1, yesterday, rawAvro1)

        cvRepository.lagreCv(cv1)

        val cv2 = cvRepository.hentCv(aktorId1)
                ?.update(aktorId1, now, rawAvro2)

        assertNotNull(cv2)
        cvRepository.lagreCv(cv2!!)

        val hentet2 = cvRepository.hentCv(aktorId1)

        assertEquals(hentet2?.aktorId, aktorId1)
        assertEquals(hentet2?.sistEndret, now)
        assertEquals(hentet2?.rawAvro, rawAvro2)
    }

    @Test
    fun `ukjent cv gir null`() {
        val ukjentCv = cvRepository.hentCv(aktorIdUkjent)

        assertNull(ukjentCv)
    }
}