package no.nav.cv

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.micronaut.test.annotation.MicronautTest
import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.RawCV
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest
class CvRepositoryTest {

    @Inject
    lateinit var cvRepository: CvRepository

    private val now = ZonedDateTime.now()
    private val yesterday = ZonedDateTime.now().minusDays(1)

    @Test
    fun `finn cv knyttet til aktorid`() {
        val cv = RawCV.create("123", now, "raw avro string")

        cvRepository.lagreCv(cv)

        val hentet = cvRepository.hentCv("123")

        assertThat(hentet).isNotNull()
        assertThat(hentet?.aktorId).isEqualTo("123")
        assertThat(hentet?.sistEndret).isEqualTo(now)
        assertThat(hentet?.rawAvro).isEqualTo("raw avro string")
    }

    @Test
    fun `finn en av flere cv`() {
        val cv1 = RawCV.create("123", now, "raw avro string 1")
        val cv2 = RawCV.create("321", yesterday, "raw avro string 2")

        cvRepository.lagreCv(cv1)
        cvRepository.lagreCv(cv2)

        val hentet1 = cvRepository.hentCv("123")

        assertThat(hentet1).isNotNull()
        assertThat(hentet1?.aktorId).isEqualTo("123")
        assertThat(hentet1?.sistEndret).isEqualTo(now)
        assertThat(hentet1?.rawAvro).isEqualTo("raw avro string 1")

        val hentet2 = cvRepository.hentCv("321")

        assertThat(hentet2).isNotNull()
        assertThat(hentet2?.aktorId).isEqualTo("321")
        assertThat(hentet2?.sistEndret).isEqualTo(yesterday)
        assertThat(hentet2?.rawAvro).isEqualTo("raw avro string 2")
    }



}