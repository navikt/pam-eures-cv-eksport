package no.nav.cv.eures.esco

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.cv.eures.esco.dto.EscoDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.*

@Service
class OntologiClient(
    @Value("\${pam-ontologi.baseurl}") private val baseUrl: String,
) {
    companion object {
        private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setTimeZone(TimeZone.getTimeZone("Europe/Oslo"))

    }

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .version(HttpClient.Version.HTTP_1_1)
        .build()

    fun hentEscoInformasjonFraOntologien(konseptId: String): EscoDTO? {
        val request = HttpRequest.newBuilder()
            .uri(URI("$baseUrl/rest/ontologi/esco/${konseptId}"))
            .header("Nav-CallId", "pam-eures-cv-eksport-${UUID.randomUUID()}")
            .timeout(Duration.ofMinutes(5))
            .GET()
            .build()

        val response = httpClient.send(request, BodyHandlers.ofString())

        if (response.statusCode() == 404) return null

        if (response.statusCode() > 300) {
            throw RuntimeException("Feil i euresoppslag med konseptid $konseptId mot pam-ontologi ${response.statusCode()} : ${response.body()}")
        }

        return objectMapper.readValue<EscoDTO>(response.body())
    }
}
