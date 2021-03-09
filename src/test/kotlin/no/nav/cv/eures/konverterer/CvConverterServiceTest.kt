package no.nav.cv.eures.konverterer

import no.nav.cv.eures.cv.CvXml
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
internal class CvConverterServiceTest {

    @Autowired
    lateinit var cvConverterService: CvConverterService

    @Test
    fun `Not changing checksum when xml is not changed`() {
        val cvBefore = createCv("Ref 1", "Fnr 1")

        val checksumBefore = cvBefore.checksum
        val cvAfter = cvConverterService.updateIfChanged(cvBefore, xmlString1)

        assertEquals(checksumBefore, cvAfter.checksum)
        assertEquals(createdAt, cvAfter.sistEndret)
    }

    @Test
    fun `Updated checksum when xml is changed`() {
        val cvBefore = createCv("Ref 2", "Fnr 2")

        val checksumBefore = cvBefore.checksum
        val checksumAfter = cvConverterService.md5(xmlString2)

        val cvAfter = cvConverterService.updateIfChanged(cvBefore, xmlString2)

        assertNotEquals(checksumBefore, cvAfter.checksum)
        assertEquals(checksumAfter, cvAfter.checksum)
        assertTrue(createdAt.isBefore(cvAfter.sistEndret))
    }


    val createdAt = ZonedDateTime.now().minusMinutes(5)

    val xmlString1 = "XML String 1"
    val xmlString2 = "XML String 2"

    private fun createCv(ref: String, fnr: String) = CvXml.create(
            reference = ref,
            foedselsnummer = fnr,
            opprettet = createdAt,
            sistEndret = createdAt,
            slettet = null,
            xml = xmlString1,
            checksum = cvConverterService.md5(xmlString1)
    )
}