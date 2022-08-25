package no.nav.cv.eures.samtykke

import no.nav.cv.eures.bruker.InnloggetBrukerService
import no.nav.cv.eures.pdl.PdlPersonGateway
import no.nav.security.token.support.test.JwtTokenGenerator
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.json.JSONStringer
import org.junit.jupiter.api.Test
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
import reactor.util.MultiValueMap

@WebMvcTest(SamtykkeController::class)
@ActiveProfiles("test")
@Import(TokenGeneratorConfiguration::class)
class SamtykkeControllerTest {

    var token = JwtTokenGenerator.createSignedJWT("12345678910").serialize()

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private val samtykkeService: SamtykkeService? = null

    @MockBean
    private val pdlPersonGateway: PdlPersonGateway? = null

    @MockBean
    private val innloggetbrukerService: InnloggetBrukerService? = null

/*
    @Test
    @Disabled("Need to implement usage of authorizastion header first")
    fun `oppdater og hent samtykke`() {

        val samtykke = Samtykke(now, personalia = true, utdanning = true)

        val body = client.postForObject("samtykke", samtykke, String::class.java)

        assertEquals("OK",body)

        val hentet = client.getForEntity("samtykke", Samtykke::class.java).body

        // TODO : Hvorfor tror databasen at den er UTC? --> Det er default for ZonedTimeDate
        assertEquals(now.withZoneSameInstant(ZoneId.of("UTC")), hentet?.sistEndret)
        assertEquals(true, hentet?.personalia)
        assertEquals(true, hentet?.utdanning)
    }
*/
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
    fun `call to get samtykke` () {
        val fnr = "111111111"
        var samtykke = Samtykke()
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
            MockMvcResultMatchers.content().json("{'personalia':false}")
        )
    }
/*
    @Test
    fun `call to post samtykke for updating` () {
        val samtykke = Samtykke()
        Mockito.`when`(innloggetbrukerService?.fodselsnummer())
            .thenReturn("111111111")
        mockMvc.perform(
            MockMvcRequestBuilders.post("/samtykke").
                .headers(headerWithToken(token))
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        )
    }
*/
    fun headerWithToken(token: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        return headers
    }

}
