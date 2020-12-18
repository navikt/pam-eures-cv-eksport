package no.nav.cv.eures.eures

import com.nhaarman.mockitokotlin2.eq
import no.nav.cv.eures.cv.CvXml
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.eures.dto.GetDetails.CandidateDetail.Status.ACTIVE
import no.nav.cv.eures.eures.dto.GetDetails.CandidateDetail.Status.CLOSED
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TokenGeneratorConfiguration::class)
class EuresServiceIntegrationTest {

    @Autowired
    lateinit var euresService: EuresService

    @Autowired
    lateinit var cvXmlRepository: CvXmlRepository

    private var oneDayAgo = ZonedDateTime.now().minusDays(1)

    private fun testData() = listOf(
            CvXml().update("PAM-1", "1234567890", oneDayAgo, oneDayAgo, null, xml = "SOME XML"),
            CvXml().update("PAM-2", "1234567891", oneDayAgo, oneDayAgo.plusHours(12), null, xml = "SOME XML"),
            CvXml().update("PAM-3", "1234567892", oneDayAgo, oneDayAgo.plusHours(12), oneDayAgo.plusDays(1), xml = "SOME XML")
    )

    private val active = listOf(testData()[0], testData()[1])

    @BeforeEach
    fun setUp() {
        euresService = EuresService(cvXmlRepository)
        //testData().forEach { cvXmlRepository.save(it) }
    }

    @Test
    @DirtiesContext
    fun `records skal endre type basert paa timestamps`() {
        var now = ZonedDateTime.now().minusHours(2)
        val candidate = cvXmlRepository.save(CvXml().update("PAM-4", "1234567893", now, now, null, xml = "SOME XML"))
        assertEquals(1, euresService.getChangedReferences(now.minusSeconds(1)).createdReferences.size)

        now = ZonedDateTime.now().plusHours(1)
        cvXmlRepository.save(candidate.update(candidate.reference, candidate.foedselsnummer, candidate.opprettet, now, null, "SOME UPDATED XML"))
        val modified = euresService.getChangedReferences(now.minusMinutes(1))
        assertEquals(0, modified.createdReferences.size)
        assertEquals(1, modified.modifiedReferences.size)
        assertEquals(0, modified.closedReferences.size)

        now = ZonedDateTime.now().plusHours(1)
        cvXmlRepository.save(candidate.update(candidate.reference, candidate.foedselsnummer, candidate.opprettet, now, now, "SOME UPDATED XML"))
        val closed = euresService.getChangedReferences(now.minusMinutes(1))
        assertEquals(0, closed.createdReferences.size)
        assertEquals(0, closed.modifiedReferences.size)
        assertEquals(1, closed.closedReferences.size)
    }

}
