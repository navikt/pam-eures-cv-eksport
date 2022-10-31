package no.nav.cv.eures.pdl

import no.nav.cv.eures.bruker.InnloggetBruker
import no.nav.cv.eures.samtykke.Samtykke
import no.nav.cv.eures.samtykke.SamtykkeService
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(EuresTilgangController::class)
@EnableMockOAuth2Server
class EuresTilgangControllerTest {

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var pdlPersonGateway: PdlPersonGateway

    @MockBean
    private lateinit var innloggetBrukerService: InnloggetBruker

    @MockBean
    private lateinit var samtykkeService: SamtykkeService

    @Test
    fun `call returns no statsborgerskap when statsborgerskap result is null` () {
        mockMvc.perform(MockMvcRequestBuilders.get("/eures/tilgang")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${generateTestToken()}")
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().string("false")
        )
    }

    @Test
    fun `call returns euEos statsborgerskap true when service returns true` () {
        Mockito.`when`(innloggetBrukerService.fodselsnummer())
            .thenReturn("111111111")
        Mockito.`when`(pdlPersonGateway.erEUEOSstatsborger(anyString()))
            .thenReturn(true)
        mockMvc.perform(MockMvcRequestBuilders.get("/eures/tilgang")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${generateTestToken()}")
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().string("true")
        )
    }

    @Test
    fun `call returns euEos statsborgerskap false when service returns false` () {
        Mockito.`when`(innloggetBrukerService.fodselsnummer())
            .thenReturn("111111111")
        Mockito.`when`(pdlPersonGateway.erEUEOSstatsborger(anyString()))
            .thenReturn(false)
        mockMvc.perform(MockMvcRequestBuilders.get("/eures/tilgang")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${generateTestToken()}")
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().string("false")
        )
    }

    @Test
    fun `call returns true when samtykke exists and borgerskap is outside Eu` () {
        Mockito.`when`(innloggetBrukerService.fodselsnummer())
            .thenReturn("111111111")
        Mockito.`when`(pdlPersonGateway.erEUEOSstatsborger(anyString()))
            .thenReturn(false)
        Mockito.`when`(samtykkeService.hentSamtykke(anyString()))
            .thenReturn(Samtykke())
        mockMvc.perform(MockMvcRequestBuilders.get("/eures/tilgang")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${generateTestToken()}")
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().string("true")
        )
    }

    @Test
    fun `call returns true when samtykke exists and borgerskap is inside Eu` () {
        Mockito.`when`(innloggetBrukerService.fodselsnummer())
            .thenReturn("111111111")
        Mockito.`when`(pdlPersonGateway.erEUEOSstatsborger(anyString()))
            .thenReturn(true)
        Mockito.`when`(samtykkeService.hentSamtykke(anyString()))
            .thenReturn(Samtykke())
        mockMvc.perform(MockMvcRequestBuilders.get("/eures/tilgang")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${generateTestToken()}")
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().string("true")
        )
    }

    private fun generateTestToken(): String {
        val token = mockOAuth2Server.issueToken(
            "selvbetjening",
            "EuresTilgangController",
            DefaultOAuth2TokenCallback(
                subject = "selvbetjening",
                audience = listOf("aud-localhost"),
                claims = mapOf("issuer" to "selvbetjening",
                    "pid" to "111111111"),
                expiry = 3600)
        )
        return token.serialize()
    }
}