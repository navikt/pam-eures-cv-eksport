package no.nav.cv.eures.konverterer.esco

import no.nav.cv.eures.esco.EscoService
import no.nav.cv.eures.esco.OntologiClient
import no.nav.cv.eures.esco.dto.EscoDTO
import no.nav.cv.eures.esco.dto.EscoKodeType
import no.nav.cv.eures.esco.dto.KonseptGrupperingDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

open class EscoServiceTest {
    private val ontologiClient = mock(OntologiClient::class.java)
    private val escoService = EscoService(ontologiClient)

    @Test
    fun `test escoService`() {
        val mockRespons = KonseptGrupperingDTO(1234, "Fin erfaring", listOf("1234"), EscoDTO("Fin erfaring ESCO","http://data.europa.eu/esco/occupation/4ad4024e-d1d3-4dea-b6d1-2c7948111dce|http://data.europa.eu/esco/isco/c1234"))
        `when`(ontologiClient.hentKonseptGrupperingFraOntologien("1234")).thenReturn(mockRespons)

        val respons = escoService.hentEscoForKonseptId("1234")

        assertEquals(2, respons.size)

        val (esco, isco) = respons.partition { it.type == EscoKodeType.ESCO }

        assertEquals(1, esco.size)
        assertEquals(1, isco.size)

        assertEquals("Fin erfaring ESCO", esco.first().label)
        assertEquals("Fin erfaring ESCO", isco.first().label)
        assertEquals("http://data.europa.eu/esco/occupation/4ad4024e-d1d3-4dea-b6d1-2c7948111dce", esco.first().kode)
        assertEquals("1234", isco.first().kode)

        val escoJobCategory = esco.first().tilJobCategoryCode()
        assertEquals("http://data.europa.eu/esco/occupation/4ad4024e-d1d3-4dea-b6d1-2c7948111dce", escoJobCategory.code)
        assertEquals("Fin erfaring ESCO", escoJobCategory.name)
        assertEquals("https://ec.europa.eu/esco/portal", escoJobCategory.listURI)
        assertEquals("https://ec.europa.eu/esco/portal", escoJobCategory.listSchemeURI)
        assertEquals("ESCO_Occupations", escoJobCategory.listName)
        assertEquals("ESCOv1.09", escoJobCategory.listVersionID)

        val iscoJobCategory = isco.first().tilJobCategoryCode()
        assertEquals("1234", iscoJobCategory.code)
        assertEquals("Fin erfaring ESCO", iscoJobCategory.name)
        assertEquals("http://ec.europa.eu/esco/ConceptScheme/ISCO2008", iscoJobCategory.listURI)
        assertEquals("http://ec.europa.eu/esco/ConceptScheme/ISCO2008", iscoJobCategory.listSchemeURI)
        assertEquals("ISCO2008", iscoJobCategory.listName)
        assertEquals("2008", iscoJobCategory.listVersionID)
    }
}
