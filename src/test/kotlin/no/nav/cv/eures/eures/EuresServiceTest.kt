package no.nav.cv.eures.eures

import com.nhaarman.mockitokotlin2.eq
import no.nav.cv.eures.cv.CvXml
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.eures.dto.GetDetails.CandidateDetail.Status.ACTIVE
import no.nav.cv.eures.eures.dto.GetDetails.CandidateDetail.Status.CLOSED
import no.nav.cv.eures.pdl.PdlPersonGateway
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.ZonedDateTime

class EuresServiceTest {

    lateinit var euresService: EuresService

    val cvXmlRepository: CvXmlRepository = Mockito.mock(CvXmlRepository::class.java)
    var samtykkeRepository : SamtykkeRepository  = Mockito.mock(SamtykkeRepository::class.java)
    var personGateway: PdlPersonGateway = Mockito.mock(PdlPersonGateway::class.java)

    private var oneDayAgo = ZonedDateTime.now().minusDays(1)
    private var pageRequest = PageRequest.of(0, 100)

    private fun testData(): Page<CvXml> = PageImpl(listOf(
            CvXml().update("PAM-1", "1234567890", oneDayAgo, oneDayAgo, null, xml = "SOME XML", checksum = "SOME CHECKSUM", null),
            CvXml().update("PAM-2", "1234567891", oneDayAgo, oneDayAgo.plusHours(12), null, xml = "SOME XML", checksum = "SOME CHECKSUM", null),
            CvXml().update("PAM-3", "1234567892", oneDayAgo, oneDayAgo.plusHours(12), oneDayAgo.plusDays(1), xml = "SOME XML", checksum = "SOME CHECKSUM", null)
    ))

    private val active = PageImpl(listOf(testData().content[0], testData().content[1]))

    @BeforeEach
    fun setUp() {
        euresService = EuresService(cvXmlRepository, samtykkeRepository, personGateway)
        //testData().forEach { cvXmlRepository.save(it) }
    }


    @Test
    fun `getAll skal returnere kun aktive referanser`() {
        Mockito.`when`(cvXmlRepository.fetchAllActive(eq(pageRequest))).thenReturn(active)
        euresService.getAllReferences().run {
            assertEquals(0, allReferences.filter { it.status == "CLOSED" }.size)
        }
    }

    @Test
    fun `getChanges skal returnere endret verdier riktig grupert etter gruppe`() {
        Mockito.`when`(cvXmlRepository.fetchAllCvsAfterTimestamp(eq(pageRequest), eq(oneDayAgo))).thenReturn(testData())
        val all = euresService.getChangedReferences(oneDayAgo)

        assertEquals(1, all.createdReferences.size)
        assertEquals(1, all.modifiedReferences.size)
        assertEquals(1, all.closedReferences.size)
        assertEquals(3, listOf(all.closedReferences, all.createdReferences, all.modifiedReferences).flatten().size)

    }

    @Test
    fun `getDetails skal returnere korrekt status paa details`() {
        val references = testData().map(CvXml::reference)

        Mockito.`when`(cvXmlRepository.fetchAllCvsByReference(eq(references.content))).thenReturn(testData().content)

        val details = euresService.getDetails(references.content)

        assertAll({
            assertTrue(details.details["PAM-1"]?.status == ACTIVE
                    && details.details["PAM-2"]?.status == ACTIVE)
            assertTrue(!details.details["PAM-1"]?.content.isNullOrBlank()
                    && !details.details["PAM-2"]?.content.isNullOrBlank())

            assertTrue(details.details["PAM-3"]?.status == CLOSED)
            assertTrue(details.details["PAM-3"]?.content.isNullOrBlank())
        })
    }


}
