package no.nav.cv.eures.eures

import no.nav.cv.eures.cv.CvXml
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.eures.dto.GetDetails.CandidateDetail.Status.ACTIVE
import no.nav.cv.eures.eures.dto.GetDetails.CandidateDetail.Status.CLOSED
import no.nav.cv.eures.samtykke.Samtykke
import no.nav.cv.eures.samtykke.SamtykkeRepository
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
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
import java.time.ZonedDateTime

@DataJpaTest(includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [Repository::class])])
@Import(TokenGeneratorConfiguration::class)
class EuresRepositoryTest {

    @Autowired
    lateinit var cvXmlRepository: CvXmlRepository

    @Autowired
    lateinit var samtykkeRepository: SamtykkeRepository

    private var oneDayAgo = ZonedDateTime.now().minusDays(1)

    private fun testData() = listOf(
            CvXml().update("PAM-1", "1234567890", oneDayAgo, oneDayAgo, null, xml = "SOME XML", checksum = "SOME CHECKSUM"),
            CvXml().update("PAM-2", "1234567891", oneDayAgo, oneDayAgo.plusHours(12), null, xml = "SOME XML", checksum = "SOME CHECKSUM"),
            CvXml().update("SLETTET", "1234567892", oneDayAgo, oneDayAgo.plusHours(12), oneDayAgo.plusDays(1), xml = "SOME XML", checksum = "SOME CHECKSUM"),
            CvXml().update("MANGLER_SAMTYKKE", "1234567893", oneDayAgo, oneDayAgo.plusHours(10), null, xml = "SOME XML", checksum = "SOME CHECKSUM")
    )

    private val fnrMedSamtykke = listOf("1234567890", "1234567891", "1234567892")


    @BeforeEach
    fun setUp() {
        testData().forEach { cvXmlRepository.save(it) }
        fnrMedSamtykke.forEach { fnr -> samtykkeRepository.oppdaterSamtykke(fnr, Samtykke()) }
    }


    @Test
    fun `fetchAllActive skal returnere kun aktive cv-xml`() {
        val active = cvXmlRepository.fetchAllActive()
        assertEquals(2, active.size)
        active.forEach { cvXml -> assertTrue(listOf("PAM-1", "PAM-2").contains(cvXml.reference)) }
    }

    @Test
    fun `getChanges skal returnere endret verdier riktig grupert etter gruppe`() {
        val all = cvXmlRepository.fetchAllCvsAfterTimestamp(oneDayAgo.minusHours(1))
        val two = cvXmlRepository.fetchAllCvsAfterTimestamp(oneDayAgo.plusHours(11))
        val one = cvXmlRepository.fetchAllCvsAfterTimestamp(oneDayAgo.plusHours(12))
        val zero = cvXmlRepository.fetchAllCvsAfterTimestamp(oneDayAgo.plusDays(1))

        assertEquals(4, all.size)
        assertEquals(2, two.size)
        assertEquals(1, one.size)
        assertEquals(0, zero.size)
    }

    @Test
    fun `getDetails skal returnere korrekt status paa details`() {
        val multipleResults = cvXmlRepository.fetchAllCvsByReference(testData().map(CvXml::reference))

        assertEquals(4, multipleResults.size)

        val single = cvXmlRepository.fetchAllCvsByReference(listOf("PAM-1"))
        assertEquals(1, single.size)

        val unknown = cvXmlRepository.fetchAllCvsByReference(listOf("NON-EXISTANT"))
        assertEquals(0, unknown.size)
    }


}
