package no.nav.cv

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.micronaut.test.annotation.MicronautTest
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.RawCV
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

        assertThat(hentet).isNotNull()
        assertThat(hentet?.aktorId).isEqualTo(aktorId1)
        assertThat(hentet?.sistEndret).isEqualTo(now)
        assertThat(hentet?.rawAvro).isEqualTo(rawAvro1)
    }

    @Test
    fun `finn en av flere cv`() {
        val cv1 = RawCV.create(aktorId1, now, rawAvro1)
        val cv2 = RawCV.create(aktorId2, yesterday, rawAvro2)

        cvRepository.lagreCv(cv1)
        cvRepository.lagreCv(cv2)

        val hentet1 = cvRepository.hentCv(aktorId1)

        assertThat(hentet1).isNotNull()
        assertThat(hentet1?.aktorId).isEqualTo(aktorId1)
        assertThat(hentet1?.sistEndret).isEqualTo(now)
        assertThat(hentet1?.rawAvro).isEqualTo(rawAvro1)

        val hentet2 = cvRepository.hentCv(aktorId2)

        assertThat(hentet2).isNotNull()
        assertThat(hentet2?.aktorId).isEqualTo(aktorId2)
        assertThat(hentet2?.sistEndret).isEqualTo(yesterday)
        assertThat(hentet2?.rawAvro).isEqualTo(rawAvro2)
    }

    @Test
    fun `cv blir oppdatert`() {
        val cv1 = RawCV.create(aktorId1, yesterday, rawAvro1)

        cvRepository.lagreCv(cv1)

        val cv2 = cvRepository.hentCv(aktorId1)
                ?.update(aktorId1, now, rawAvro2)

        assertThat(cv2).isNotNull()
        cvRepository.lagreCv(cv2!!)

        val hentet2 = cvRepository.hentCv(aktorId1)

        assertThat(hentet2?.aktorId).isEqualTo(aktorId1)
        assertThat(hentet2?.sistEndret).isEqualTo(now)
        assertThat(hentet2?.rawAvro).isEqualTo(rawAvro2)
    }

    @Test
    fun `ukjent cv gir null`() {
        val ukjentCv = cvRepository.hentCv(aktorIdUkjent)

        assertThat(ukjentCv).isNull()
    }
}