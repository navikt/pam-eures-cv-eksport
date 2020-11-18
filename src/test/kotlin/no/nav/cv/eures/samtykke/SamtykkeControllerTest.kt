package no.nav.cv.eures.samtykke

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.mockk
import no.nav.cv.eures.konverterer.CvConverterService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.time.ZoneId
import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles("test")
class SamtykkeControllerTest {

    private val aktoerId1 = "123"
    private val aktoerId2 = "321"

    private val now = ZonedDateTime.now()
    private val yesterday = now.minusDays(1)

    private val client = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:9030/pam-eures-cv-eksport/")
            .build()

    @Autowired
    lateinit var samtykkeRepository: SamtykkeRepository

    @MockBean
    lateinit var konverterer: CvConverterService


    @Test
    @Disabled("Need to implement usage of authorizastion header first")
    fun `oppdater og hent samtykke`() {

        val samtykke = Samtykke(now, personalia = true, utdanning = true)

        val body = client
                .post()
                .uri("samtykke/")
                .body(BodyInserters.fromValue(samtykke))
                .exchange()
                .expectBody().returnResult().responseBodyContent
                .let { String(it) }

        assertEquals("OK",body)

        val hentet = client
                .get()
                .uri("samtykke/")
                .exchange()
                .expectBody().returnResult().requestBodyContent
                .let { ObjectMapper().readValue(it, Samtykke::class.java) }

        // TODO : Hvorfor tror databasen at den er UTC? --> Det er default for ZonedTimeDate
        assertEquals(now.withZoneSameInstant(ZoneId.of("UTC")), hentet?.sistEndret)
        assertEquals(true, hentet?.personalia)
        assertEquals(true, hentet?.utdanning)
    }

}
