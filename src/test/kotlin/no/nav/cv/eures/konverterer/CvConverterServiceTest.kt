package no.nav.cv.eures.konverterer

import no.nav.cv.eures.cv.CvXml
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime

@SpringBootTest
internal class CvConverterServiceTest {

    @Autowired
    lateinit var cvConverterService: CvConverterService

    @Test
    fun `Not changing checksum when xml is not changed`() {

        val cvBefore = getCv("Aktoer Id 1")
        val checksumBefore = cvBefore.checksum
        val cvAfter = cvConverterService.updateIfChanged(cvBefore, xmlString1)

        assertEquals(checksumBefore, cvAfter.checksum)
        assertEquals(createdAt, cvAfter.sistEndret)
    }

    @Test
    fun `Updated checksum when xml is changed`() {
        val cvBefore = getCv("Aktoer Id 2")
        val checksumBefore = cvBefore.checksum

        val cvAfter = cvConverterService.updateIfChanged(cvBefore, xmlString2)

        val checksum2 = cvConverterService.md5(xmlString2)

        assertNotEquals(checksumBefore, cvAfter.checksum)
        assertEquals(checksum2, cvAfter.checksum)
        assertTrue(createdAt.isBefore(cvAfter.sistEndret))
    }


    val createdAt = ZonedDateTime.now().minusMinutes(5)

    val xmlString1 = "XML String 1"
    val xmlString2 = "XML String 2"

    fun getCv(aid: String) = CvXml.create(
        reference = "Test",
        aktoerId = aid,
        opprettet = createdAt,
        sistEndret = createdAt ,
        slettet = null,
        xml = xmlString1,
        checksum = cvConverterService.md5(xmlString1)
    )
}