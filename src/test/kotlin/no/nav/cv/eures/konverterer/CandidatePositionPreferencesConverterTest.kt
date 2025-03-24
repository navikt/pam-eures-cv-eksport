package no.nav.cv.eures.konverterer

import no.nav.cv.dto.jobwishes.*
import no.nav.cv.eures.esco.EscoService
import no.nav.cv.eures.esco.OntologiClient
import no.nav.cv.eures.esco.dto.EscoDTO
import no.nav.cv.eures.model.PositionOfferingTypes.*
import no.nav.cv.eures.model.PositionSchedule.*
import no.nav.cv.eures.samtykke.Samtykke
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class CandidatePositionPreferencesConverterTest {
    private val ontologiClient = mock(OntologiClient::class.java)
    private val escoService = EscoService(ontologiClient)

    @Test
    fun `test konvertering til xml`() {
        val mockResponsEsco = EscoDTO("Fin erfaring ESCO", "http://data.europa.eu/esco/occupation/4ad4024e-d1d3-4dea-b6d1-2c7948111dce")
        val mockResponsIsco = EscoDTO("Fin erfaring ISCO", "http://data.europa.eu/esco/isco/c1234")

        `when`(ontologiClient.hentEscoInformasjonFraOntologien("1234")).thenReturn(mockResponsIsco)
        `when`(ontologiClient.hentEscoInformasjonFraOntologien("12345")).thenReturn(mockResponsEsco)


        val samtykke = Samtykke(personalia = true, jobbonsker = true, land = listOf("DE", "NO"))
        val jobbønsker = lagJobbønsker(occupations = lagJobber(1234, 12345), occupationTypes = lagAnsettelsesform(Ansettelsesform.FAST, Ansettelsesform.FERIEJOBB, Ansettelsesform.SESONG), workloadTypes = lagOmfang(Omfang.HELTID, Omfang.DELTID, Omfang.HELTID))

        val converter = CandidatePositionPreferencesConverter(samtykke, jobbønsker, escoService)

        val (preferredLocation, jobCategory, positionOffering, positionSchedule) = converter.toXmlRepresentation()

        assertEquals(2, preferredLocation!!.size)
        assertEquals("DE", preferredLocation[0].referenceLocation.countryCode)
        assertEquals("NO", preferredLocation[1].referenceLocation.countryCode)

        assertEquals(2, jobCategory!!.size)
        assertEquals(1, jobCategory.filter { it.jobCategoryCode.code == "http://data.europa.eu/esco/occupation/4ad4024e-d1d3-4dea-b6d1-2c7948111dce" }.size)
        assertEquals(1, jobCategory.filter { it.jobCategoryCode.code == "1234" }.size)

        assertEquals(2, positionOffering!!.size)
        assertEquals(1, positionOffering.filter { it.code == Seasonal.name }.size)
        assertEquals(1, positionOffering.filter { it.code == DirectHire.name }.size)
        assertEquals(0, positionOffering.filter { it.code == Temporary.name }.size)

        assertEquals(2, positionSchedule!!.size)
        assertEquals(1, positionSchedule.filter { it.code == FullTime.name }.size)
        assertEquals(1, positionSchedule.filter { it.code == PartTime.name }.size)
        assertEquals(0, positionSchedule.filter { it.code == FlexTime.name }.size)
    }

    @Test
    fun `test konvertering til xml uten samtykke for jobbønsker`() {
        val samtykke = Samtykke(personalia = true, jobbonsker = false, land = listOf("DE", "NO"))
        val jobbønsker = lagJobbønsker(occupationTypes = lagAnsettelsesform(Ansettelsesform.FAST, Ansettelsesform.FERIEJOBB, Ansettelsesform.SESONG), workloadTypes = lagOmfang(Omfang.HELTID, Omfang.DELTID, Omfang.HELTID))

        val converter = CandidatePositionPreferencesConverter(samtykke, jobbønsker, escoService)

        val (preferredLocation, jobCategory, positionOffering, positionSchedule) = converter.toXmlRepresentation()

        assertEquals(2, preferredLocation!!.size)
        assertEquals("DE", preferredLocation[0].referenceLocation.countryCode)
        assertEquals("NO", preferredLocation[1].referenceLocation.countryCode)

        assertNull(jobCategory)
        assertNull(positionSchedule)
        assertNull(positionOffering)
    }

    private fun lagJobbønsker(
        occupations: List<CvEndretInternOccupation> = emptyList(),
        occupationTypes: List<CvEndretInternOccupationType> = emptyList(),
        workloadTypes: List<CvEndretInternWorkLoadType> = emptyList()
    ) = CvEndretInternJobwishesDto(
        id = 1,
        startOption = null,
        occupations = occupations,
        occupationDrafts = emptyList(),
        skills = emptyList(),
        locations = emptyList(),
        occupationTypes = occupationTypes,
        workTimes = emptyList(),
        workDays = emptyList(),
        workShiftTypes = emptyList(),
        workLoadTypes = workloadTypes,
        createdAt = null,
        updatedAt = null
    )

    private fun lagAnsettelsesform(vararg ansettelsesformer: Ansettelsesform) = ansettelsesformer.map { CvEndretInternOccupationType(title = it) }
    private fun lagOmfang(vararg omfang: Omfang) = omfang.map { CvEndretInternWorkLoadType(title = it) }
    private fun lagJobber(vararg konseptIder: Long) = konseptIder.map { CvEndretInternOccupation(it.toString(), it) }
}
