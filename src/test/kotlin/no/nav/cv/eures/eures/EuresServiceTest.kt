package no.nav.cv.eures.eures

import io.micronaut.test.annotation.MicronautTest
import no.nav.cv.eures.cv.CvXml
import no.nav.cv.eures.cv.CvXmlRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertAll
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest
@TestInstance(Lifecycle.PER_CLASS)
class EuresServiceTest {

    @Inject
    lateinit var euresService: EuresService

    @Inject
    lateinit var cvXmlRepository: CvXmlRepository

    private var oneDayAgo = ZonedDateTime.now().minusDays(1)

    private fun testData() = listOf(
            CvXml().update("PAM-1", "1234567890", oneDayAgo, oneDayAgo, null, xml = "SOME XML"),
            CvXml().update("PAM-2", "1234567891", oneDayAgo, oneDayAgo.plusHours(12), null, xml = "SOME XML"),
            CvXml().update("PAM-3", "1234567892", oneDayAgo, oneDayAgo.plusHours(12), oneDayAgo.plusDays(1), xml = "SOME XML")
    )


    @BeforeAll
    fun setUp() {
        testData().forEach { cvXmlRepository.save(it) }
    }


    @Test
    fun `getAll skal returnere kun aktive referanser`() {
        euresService.getAllReferences().run {
            assertEquals(0, allReferences.filter { it.status == "CLOSED" }.size)
        }
    }

    @Test
    fun `getChanges skal returnere endret verdier riktig grupert etter gruppe`() {
        val all = euresService.getChangedReferences(oneDayAgo.minusHours(1))
        val two = euresService.getChangedReferences(oneDayAgo.plusHours(11))
        val one = euresService.getChangedReferences(oneDayAgo.plusHours(12))
        val zero = euresService.getChangedReferences(oneDayAgo.plusDays(1))

        assertAll({
            assertEquals(1, all.createdReferences.size)
            assertEquals(1, all.modifiedReferences.size)
            assertEquals(1, all.closedReferences.size)
            assertEquals(0, two.createdReferences.size)
            assertEquals(1, two.modifiedReferences.size)
            assertEquals(1, two.closedReferences.size)
            assertEquals(0, one.createdReferences.size)
            assertEquals(0, one.modifiedReferences.size)
            assertEquals(1, one.closedReferences.size)
            assertEquals(0, zero.createdReferences.size)
            assertEquals(0, zero.modifiedReferences.size)
            assertEquals(0, zero.closedReferences.size)
            assertEquals(3, listOf(all.closedReferences, all.createdReferences, all.modifiedReferences).flatten().size)
            assertEquals(2, listOf(two.closedReferences, two.createdReferences, two.modifiedReferences).flatten().size)
            assertEquals(1, listOf(one.closedReferences, one.createdReferences, one.modifiedReferences).flatten().size)
            assertEquals(0, listOf(zero.closedReferences, zero.createdReferences, zero.modifiedReferences).flatten().size)
        })

    }

    @Test
    fun `getDetails skal returnere korrekt status paa details`() {
        val details = euresService.getDetails(testData().map(CvXml::reference))

        assertAll({
            assertTrue(details.details["PAM-1"]?.status == "ACTIVE"
                    && details.details["PAM-2"]?.status == "ACTIVE")
            assertTrue(!details.details["PAM-1"]?.content.isNullOrBlank()
                    && !details.details["PAM-2"]?.content.isNullOrBlank())

            assertTrue(details.details["PAM-3"]?.status == "CLOSED")
            assertTrue(details.details["PAM-3"]?.content.isNullOrBlank())
        })
    }

    @Test
    fun `records skal endre type basert paa timestamps`() {
        var now = ZonedDateTime.now().minusHours(2)
        val candidate = cvXmlRepository.save(CvXml().update("PAM-4", "1234567893", now, now, null, xml = "SOME XML"))
        assertEquals(1, euresService.getChangedReferences(now.minusSeconds(1)).createdReferences.size)

        now = ZonedDateTime.now().plusHours(1)
        cvXmlRepository.save(candidate.update(candidate.reference, candidate.aktoerId, candidate.opprettet, now, null, "SOME UPDATED XML"))
        val modified = euresService.getChangedReferences(now.minusMinutes(1))
        assertEquals(0, modified.createdReferences.size)
        assertEquals(1, modified.modifiedReferences.size)
        assertEquals(0, modified.closedReferences.size)

        now = ZonedDateTime.now().plusHours(1)
        cvXmlRepository.save(candidate.update(candidate.reference, candidate.aktoerId, candidate.opprettet, now, now, "SOME UPDATED XML"))
        val closed = euresService.getChangedReferences(now.minusMinutes(1))
        assertEquals(0, closed.createdReferences.size)
        assertEquals(0, closed.modifiedReferences.size)
        assertEquals(1, closed.closedReferences.size)
    }

}