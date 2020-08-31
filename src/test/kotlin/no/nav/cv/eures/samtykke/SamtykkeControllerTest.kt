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
    fun `hent et eksisterende samtykke`() {
        val samtykke = Samtykke(aktoerId1, now, personalia = true, utdanning = true)

        samtykkeRepository.oppdaterSamtykke(samtykke)

        val request = HttpRequest.GET<String>("samtykke/$aktoerId1")

        val hentet = client.toBlocking().retrieve(request) as Samtykke


        assertEquals(aktoerId1, hentet.aktoerId)
        assertEquals(now, hentet.sistEndret)
        assertEquals(true, hentet.personalia)
        assertEquals(true, hentet.utdanning)    }

    @Test
    fun `oppdater og hent samtykke`() {

        val samtykke = Samtykke(aktoerId1, now, personalia = true, utdanning = true)

        val request = HttpRequest.POST("samtykke/$aktoerId1", samtykke)

        val body = client.toBlocking().retrieve(request)

        assertEquals("OK",body)

        val hentet = samtykkeRepository.hentSamtykke(aktoerId1)

        assertEquals(aktoerId1, hentet?.aktoerId)
        assertEquals(now, hentet?.sistEndret)
        assertEquals(true, hentet?.personalia)
        assertEquals(true, hentet?.utdanning)
    }

}