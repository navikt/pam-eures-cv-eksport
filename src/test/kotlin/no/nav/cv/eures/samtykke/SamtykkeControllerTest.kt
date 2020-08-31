package no.nav.cv.eures.samtykke

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
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

    @Test
    fun `oppdater og hent samtykke`() {

        val samtykke = Samtykke(aktoerId1, now, personalia = true, utdanning = true)

        val oppdaterRequest = HttpRequest.POST("samtykke/$aktoerId1", samtykke)

        val body = client.toBlocking().retrieve(oppdaterRequest)

        assertEquals("OK",body)

        val hentRequest = HttpRequest.GET<String>("samtykke/$aktoerId1")

        val hentet = client.toBlocking().retrieve(hentRequest, Samtykke::class.java)

        assertEquals(aktoerId1, hentet?.aktoerId)
        //assertEquals(now, hentet?.sistEndret) // TODO : Hvorfor tror databasen at den er UTC?
        assertEquals(true, hentet?.personalia)
        assertEquals(true, hentet?.utdanning)
    }

}