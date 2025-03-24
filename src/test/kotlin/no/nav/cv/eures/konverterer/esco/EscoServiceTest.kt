package no.nav.cv.eures.konverterer.esco

import no.nav.cv.eures.esco.EscoService
import no.nav.cv.eures.esco.OntologiClient
import no.nav.cv.eures.esco.dto.EscoDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

open class EscoServiceTest {
    private val ontologiClient = mock(OntologiClient::class.java)
    private val escoService = EscoService(ontologiClient)

    @Test
    fun `test escoService`() {
        val mockResponsEsco =
            EscoDTO("Fin erfaring ESCO", "http://data.europa.eu/esco/occupation/4ad4024e-d1d3-4dea-b6d1-2c7948111dce")
        val mockResponsIsco = EscoDTO("Fin erfaring ISCO", "http://data.europa.eu/esco/isco/c1234")

        `when`(ontologiClient.hentEscoInformasjonFraOntologien("1234")).thenReturn(mockResponsIsco)
        `when`(ontologiClient.hentEscoInformasjonFraOntologien("1234e")).thenReturn(mockResponsEsco)

        val responsEsco = escoService.hentEscoForKonseptId("1234e")
        val responsIsco = escoService.hentEscoForKonseptId("1234")

        assertEquals("Fin erfaring ESCO", responsEsco?.label)
        assertEquals("http://data.europa.eu/esco/occupation/4ad4024e-d1d3-4dea-b6d1-2c7948111dce", responsEsco?.kode)
        assertEquals("Fin erfaring ISCO", responsIsco?.label)
        assertEquals("1234", responsIsco?.kode)

        val escoJobCategory = responsEsco!!.tilJobCategoryCode()

        assertEquals("http://data.europa.eu/esco/occupation/4ad4024e-d1d3-4dea-b6d1-2c7948111dce", escoJobCategory.code)
        assertEquals("Fin erfaring ESCO", escoJobCategory.name)
        assertEquals("https://ec.europa.eu/esco/portal", escoJobCategory.listURI)
        assertEquals("https://ec.europa.eu/esco/portal", escoJobCategory.listSchemeURI)
        assertEquals("ESCO_Occupations", escoJobCategory.listName)
        assertEquals("ESCOv1.09", escoJobCategory.listVersionID)

        val iscoJobCategory = responsIsco!!.tilJobCategoryCode()
        assertEquals("1234", iscoJobCategory.code)
        assertEquals("Fin erfaring ISCO", iscoJobCategory.name)
        assertEquals("http://ec.europa.eu/esco/ConceptScheme/ISCO2008", iscoJobCategory.listURI)
        assertEquals("http://ec.europa.eu/esco/ConceptScheme/ISCO2008", iscoJobCategory.listSchemeURI)
        assertEquals("ISCO2008", iscoJobCategory.listName)
        assertEquals("2008", iscoJobCategory.listVersionID)
    }
}
