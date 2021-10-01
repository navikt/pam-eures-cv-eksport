package no.nav.cv.eures.eures

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.reactive.function.client.WebClient

@Disabled
@WebMvcTest(EuresController::class)
@ActiveProfiles("test")
@Import(TokenGeneratorConfiguration::class)
class EuresControllerTest {

    companion object {
        const val EURES_REQUIRED_PING_CONSTANT = "Hello from Input API"
        const val VALID_TEST_TOKEN_BASE64 = "RVVSRVMgc3VwZXIgdGVzdHNlY3JldA=="
        const val INVALID_TEST_TOKEN = "SU5WQUxJRF9URVNUX1RPS0VOCg=="
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var euresService: EuresService

    @BeforeEach
    fun setup() {
        //baseUrl = "http://localhost:${randomServerPort}/pam-eures-cv-eksport/"
    }

    @Test
    fun `call to fetch changes` () {
        mockMvc.perform(
             MockMvcRequestBuilders.get("/input/api/cv/v1.0/getChanges/1607963578952")
                     .headers(headerWithToken(VALID_TEST_TOKEN_BASE64))
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        )
    }

    @Test
    fun `call to fetch details` () {
        val requestBody = """ ["FD100003", "FD1234123"] """
        mockMvc.perform(
                MockMvcRequestBuilders.post("/input/api/cv/v1.0/getDetails")
                        .headers(headerWithToken(VALID_TEST_TOKEN_BASE64))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        )
    }

    @Test
    fun `ping controller skal inkludere constant string i returnert verdi` () {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/input/api/cv/v1.0/ping")
                        .headers(headerWithToken(VALID_TEST_TOKEN_BASE64))
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().string(EURES_REQUIRED_PING_CONSTANT)
        )
    }

    @Test
    fun `kall uten token avvises` () {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/input/api/cv/v1.0/ping")
        ).andExpect(
                MockMvcResultMatchers.status().isUnauthorized
        )
    }

    @Test
    fun `kall med ugyldig token avvises` () {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/input/api/cv/v1.0/ping")
                        .headers(headerWithToken(INVALID_TEST_TOKEN))
        ).andExpect(
                MockMvcResultMatchers.status().isUnauthorized
        )
    }

    fun headerWithToken(token: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        return headers
    }

}
