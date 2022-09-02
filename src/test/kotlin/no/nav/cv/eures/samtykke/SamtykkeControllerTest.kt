package no.nav.cv.eures.samtykke

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.nimbusds.jwt.JWTClaimNames.ISSUER
import no.nav.cv.eures.bruker.InnloggetBrukerService
import no.nav.cv.eures.pdl.PdlPersonGateway
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


@ActiveProfiles("test")
@EnableMockOAuth2Server
class SamtykkeControllerTest {

    @Autowired
    private var mockOAuth2Server: MockOAuth2Server? = null

    var token = generateTestToken()

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private val samtykkeService: SamtykkeService? = null

    @MockBean
    private val pdlPersonGateway: PdlPersonGateway? = null

    @MockBean
    private val innloggetbrukerService: InnloggetBrukerService? = null

    private fun generateTestToken(): String {
        val token = mockOAuth2Server?.issueToken(ISSUER, "aud-localhost", "aud-localhost")
        return "Bearer " + token?.serialize()
    }

    @Test
    fun `call to get not found when no previous samtykke` () {
        Mockito.`when`(innloggetbrukerService?.fodselsnummer())
        .thenReturn("111111111")
        mockMvc.perform(
            MockMvcRequestBuilders.get("/samtykke")
            .headers(headerWithToken(token))
        ).andExpect(
            MockMvcResultMatchers.status().isNotFound
        )
    }

    @Test
    fun `call to delete samtykke` () {
        Mockito.`when`(innloggetbrukerService?.fodselsnummer())
        .thenReturn("111111111")
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/samtykke")
            .headers(headerWithToken(token))
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        )
    }

    @Test
    fun `call to get samtykke` () {
        val fnr = "111111111"
        val samtykke = Samtykke()
        Mockito.`when`(innloggetbrukerService?.fodselsnummer())
        .thenReturn(fnr)
        Mockito.`when`(samtykkeService?.hentSamtykke(fnr))
        .thenReturn(samtykke)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/samtykke")
            .headers(headerWithToken(token))
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().json("{\"personalia\":false}")
        )
    }

    @Test
    fun `call to post samtykke for updating and return 451 because of no eueusstatsborgerskap` () {
        val samtykke = Samtykke()

        Mockito.`when`(innloggetbrukerService?.fodselsnummer())
            .thenReturn("111111111")
        mockMvc.perform(
            MockMvcRequestBuilders.post("/samtykke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(samtykke))
                .headers(headerWithToken(token))
        ).andExpect(
            MockMvcResultMatchers.status().isUnavailableForLegalReasons
        )
    }

    @Test
    fun `call to post samtykke return 200 ok and statsborgerskap` () {
        val samtykke = Samtykke()
        val fnr="111111111"

        Mockito.`when`(innloggetbrukerService?.fodselsnummer())
            .thenReturn(fnr)
        Mockito.`when`(pdlPersonGateway?.erEUEOSstatsborger(fnr))
            .thenReturn(true)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/samtykke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(samtykke))
                .headers(headerWithToken(token))
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().json("{\"personalia\":false}")
        )
    }
    fun asJsonString(obj: Any?): String? {
        return try {
            ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule()).writeValueAsString(obj)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun headerWithToken(token: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        return headers
    }

}
