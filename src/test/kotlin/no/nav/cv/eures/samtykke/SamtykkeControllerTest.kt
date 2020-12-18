package no.nav.cv.eures.samtykke

import no.nav.cv.eures.konverterer.CvConverterService
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.ZoneId
import java.time.ZonedDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TokenGeneratorConfiguration::class)
class SamtykkeControllerTest {

    private val aktoerId1 = "123"
    private val aktoerId2 = "321"

    private val now = ZonedDateTime.now()
    private val yesterday = now.minusDays(1)

    @LocalServerPort
    private var randomServerPort = 0
    private var baseUrl = ""
    private val client = TestRestTemplate()

    @BeforeEach
    fun setup() {
        baseUrl = "http://localhost:${randomServerPort}/pam-eures-cv-eksport/"
    }
    @Autowired
    lateinit var samtykkeRepository: SamtykkeRepository

    @MockBean
    lateinit var konverterer: CvConverterService


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

}
