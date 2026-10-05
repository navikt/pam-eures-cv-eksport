package no.nav.cv.eures.eures

import no.nav.cv.eures.util.jsonMapper

import no.nav.cv.eures.eures.dto.GetChangedReferences
import no.nav.cv.eures.eures.dto.GetDetails
import no.nav.cv.eures.model.Converters.toUtcZonedDateTime
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(EuresController::class)
@EnableMockOAuth2Server
class EuresControllerTest {

    companion object {
        const val EURES_REQUIRED_PING_CONSTANT = "Hello from Input API"
        const val VALID_TEST_TOKEN_BASE64 = "RVVSRVMgc3VwZXIgdGVzdHNlY3JldA=="
        const val INVALID_TEST_TOKEN = "SU5WQUxJRF9URVNUX1RPS0VOCg=="
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var euresService: EuresService

    @Test
    fun `call to fetch changes` () {
        val modificationTimestamp = 1607963578952.toUtcZonedDateTime()

        Mockito.`when`(euresService.getChangedReferences(any())).thenReturn(GetChangedReferences())
        mockMvc.perform(
             MockMvcRequestBuilders.get("/input/api/cv/v1.0/getChanges/1607963578952")
                     .headers(headerWithToken(VALID_TEST_TOKEN_BASE64))
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        )
        verify(euresService, times(1)).getChangedReferences(modificationTimestamp)
    }

    @Test
    fun `call to fetch details` () {
        val references = listOf("FD100003", "FD1234123")
        mockMvc.perform(
                MockMvcRequestBuilders.post("/input/api/cv/v1.0/getDetails")
                        .headers(headerWithToken(VALID_TEST_TOKEN_BASE64))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(references))
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        )
        verify(euresService, times(1)).getDetails(references)
    }

    @Test
    fun `details response preserves field names and omits null values`() {
        Mockito.`when`(euresService.getDetails(listOf("FD100003"))).thenReturn(
            GetDetails(mapOf("FD100003" to GetDetails.CandidateDetail(
                creationTimestamp = 1607963578952,
                reference = "FD100003",
                status = GetDetails.CandidateDetail.Status.ACTIVE,
                content = "<Candidate/>",
            )))
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/input/api/cv/v1.0/getDetails")
                .headers(headerWithToken(VALID_TEST_TOKEN_BASE64))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""["FD100003"]""")
        ).andExpect(
            MockMvcResultMatchers.status().isOk
        ).andExpect(
            MockMvcResultMatchers.content().json(
                """{"details":{"FD100003":{"creationTimestamp":1607963578952,"reference":"FD100003","status":"ACTIVE","content":"<Candidate/>","source":"NAV","contentFormatVersion":"1.3"}}}"""
            )
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.details.FD100003.closingTimestamp").doesNotExist()
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.details.FD100003.lastModificationTimestamp").doesNotExist()
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
        verifyNoInteractions(euresService)
    }

    @Test
    fun `kall med ugyldig token avvises` () {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/input/api/cv/v1.0/ping")
                        .headers(headerWithToken(INVALID_TEST_TOKEN))
        ).andExpect(
                MockMvcResultMatchers.status().isUnauthorized
        )
        verifyNoInteractions(euresService)
    }
    private fun asJsonString(obj: Any): String {
        return try {
            jsonMapper().writeValueAsString(obj)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun headerWithToken(token: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        return headers
    }

}
