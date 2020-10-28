package no.nav.cv.eures.samtykke

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.mockk
import no.nav.cv.eures.konverterer.CvConverterService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest
class SamtykkeControllerTest(
        private val samtykkeRepository: SamtykkeRepository
) {

    private val aktoerId1 = "123"
    private val aktoerId2 = "321"

    private val now = ZonedDateTime.now()
    private val yesterday = now.minusDays(1)

    @Inject @field:Client("/pam-eures-cv-eksport/") lateinit var client: RxHttpClient

    @MockBean(CvConverterService::class)
    fun konverterer(): CvConverterService = mockk(relaxed = true)


    @Test
    @Disabled("Need to implement usage of authorizastion header first")
    fun `oppdater og hent samtykke`() {

        val samtykke = Samtykke(now, personalia = true, utdanning = true)

        val oppdaterRequest = HttpRequest.POST("samtykke/", samtykke)

        val body = client.toBlocking().retrieve(oppdaterRequest)

        assertEquals("OK",body)

        val hentRequest = HttpRequest.GET<String>("samtykke/")

        val hentet = client.toBlocking().retrieve(hentRequest, Samtykke::class.java)

        // TODO : Hvorfor tror databasen at den er UTC? --> Det er default for ZonedTimeDate
        assertEquals(now.withZoneSameInstant(ZoneId.of("UTC")), hentet?.sistEndret)
        assertEquals(true, hentet?.personalia)
        assertEquals(true, hentet?.utdanning)
    }

}
