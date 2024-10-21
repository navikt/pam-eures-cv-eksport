package no.nav.cv.eures.samtykke

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.cv.eures.bruker.InnloggetBrukerService
import no.nav.cv.eures.pdl.PdlPersonGateway
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


@WebMvcTest(SamtykkeController::class)
@EnableMockOAuth2Server
class SamtykkeControllerTest {

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var samtykkeService: SamtykkeService

    @MockBean
    private lateinit var pdlPersonGateway: PdlPersonGateway

    @MockBean
    private lateinit var innloggetbrukerService: InnloggetBrukerService

    @Test
    fun `call to get not found when no previous samtykke` () {
        Mockito.`when`(innloggetbrukerService.fodselsnummer())
            .thenReturn("111111111")
        mockMvc.perform(
            MockMvcRequestBuilders.get("/samtykke")
                .headers(headerWithToken())
        ).andExpect(
            MockMvcResultMatchers.status().isNotFound
        )
        verify(innloggetbrukerService, times(1)).fodselsnummer()
        verify(samtykkeService, times(1)).hentSamtykke("111111111")
    }

    @Test
    fun `call to delete samtykke` () {
        Mockito.`when`(innloggetbrukerService.fodselsnummer())
            .thenReturn("111111111")
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/samtykke")
                .headers(headerWithToken())
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        )
        verify(innloggetbrukerService, times(1)).fodselsnummer()
        verify(samtykkeService, times(1)).slettSamtykke("111111111")
    }

    @Test
    fun `call to get samtykke` () {
        val fnr = "111111111"
        val samtykke = Samtykke()
        Mockito.`when`(innloggetbrukerService.fodselsnummer())
            .thenReturn(fnr)
        Mockito.`when`(samtykkeService.hentSamtykke(fnr))
            .thenReturn(samtykke)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/samtykke")
                .headers(headerWithToken())
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().json("{\"personalia\":false}")
        )

        verify(innloggetbrukerService, times(1)).fodselsnummer()
        verify(samtykkeService, times(1)).hentSamtykke("111111111")
    }

    @Test
    fun `call to post samtykke for updating and return 451 because of no eueusstatsborgerskap` () {
        val samtykke = Samtykke()
        val fnr="111111111"

        Mockito.`when`(innloggetbrukerService.fodselsnummer())
            .thenReturn(fnr)
        Mockito.`when`(pdlPersonGateway.erEUEOSstatsborger(fnr))
            .thenReturn(false)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/samtykke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(samtykke))
                .headers(headerWithToken())
        ).andExpect(
            MockMvcResultMatchers.status().isUnavailableForLegalReasons
        )
        verify(innloggetbrukerService, times(1)).fodselsnummer()
        verify(pdlPersonGateway, times(1)).erEUEOSstatsborger(fnr)
        Mockito.verifyNoInteractions(samtykkeService)
    }

    @Test
    fun `call to post samtykke return 200 ok and statsborgerskap` () {
        val samtykke = Samtykke()
        val fnr="111111111"

        Mockito.`when`(innloggetbrukerService.fodselsnummer())
            .thenReturn(fnr)
        Mockito.`when`(pdlPersonGateway.erEUEOSstatsborger(fnr))
            .thenReturn(true)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/samtykke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(samtykke))
                .headers(headerWithToken())
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().json("{\"personalia\":false}")
        )
    }
    private fun asJsonString(obj: Any): String {
        return try {
            ObjectMapper().registerModule(
                KotlinModule.Builder().build()
            ).registerModule(JavaTimeModule()).writeValueAsString(obj)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun generateTestToken(): String {
        val token = mockOAuth2Server.issueToken(
            "tokenx",
            "EuresTilgangController",
            DefaultOAuth2TokenCallback(
                subject = "tokenx",
                audience = listOf("someaudience"),
                claims = mapOf("issuer" to "tokenx",
                    "pid" to "111111111"),
                expiry = 3600)
        )
        return token.serialize()
    }

    private fun headerWithToken(): HttpHeaders {
        val headers = HttpHeaders()
        headers.setBearerAuth(generateTestToken())
        return headers
    }
}
