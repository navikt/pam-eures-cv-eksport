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
        samtykkeRepository: SamtykkeRepository
) {

    private val aktoerId1 = "123"
    private val aktoerId2 = "321"

    private val now = ZonedDateTime.now()
    private val yesterday = now.minusDays(1)

    @Inject @field:Client("/samtykke") lateinit var client: RxHttpClient

    @Test
    fun `oppdater og hent samtykke`() {

        val samtykke = Samtykke(aktoerId1, now, personalia = true, utdanning = true)

        val request = HttpRequest.POST("/samtykke/$aktoerId1", samtykke)

        val body = client.toBlocking().retrieve(request)

        assertNotNull(body)
    }

}