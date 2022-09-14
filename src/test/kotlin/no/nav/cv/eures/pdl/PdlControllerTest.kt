package no.nav.cv.eures.pdl

import no.nav.cv.eures.bruker.InnloggetBruker
import no.nav.security.token.support.test.JwtTokenGenerator
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(PdlController::class)
@ActiveProfiles("test")
@Import(TokenGeneratorConfiguration::class)
class PdlControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private val pdlPersonGateway: PdlPersonGateway? = null

    @MockBean
    private val innloggetBrukerService: InnloggetBruker? = null

    var token = JwtTokenGenerator.createSignedJWT("12345678910").serialize()


    @Test
    fun `call returns no statsborgerskap when statsborgerskap result is null` () {
        mockMvc.perform(MockMvcRequestBuilders.get("/pdl")
            .headers(headerWithToken(token))
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().string("{\"erEUEOSborger\":false}")
        )
    }

    @Test
    fun `call returns euEos statsborgerskap true when service returns true` () {
        Mockito.`when`(innloggetBrukerService?.fodselsnummer())
            .thenReturn("111111111")
        Mockito.`when`(pdlPersonGateway?.erEUEOSstatsborger(anyString()))
            .thenReturn(true)
        mockMvc.perform(MockMvcRequestBuilders.get("/pdl")
            .headers(headerWithToken(token))
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().string("{\"erEUEOSborger\":true}")
        )
    }

    @Test
    fun `call returns euEos statsborgerskap false when service returns false` () {
        Mockito.`when`(innloggetBrukerService?.fodselsnummer())
            .thenReturn("111111111")
        Mockito.`when`(pdlPersonGateway?.erEUEOSstatsborger(anyString()))
            .thenReturn(false)
        mockMvc.perform(MockMvcRequestBuilders.get("/pdl")
            .headers(headerWithToken(token))
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().string("{\"erEUEOSborger\":false}")
        )
    }

    fun headerWithToken(token: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        return headers
    }
}