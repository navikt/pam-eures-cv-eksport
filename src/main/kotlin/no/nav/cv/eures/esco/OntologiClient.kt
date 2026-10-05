package no.nav.cv.eures.esco

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.module.kotlin.readValue
import no.nav.cv.eures.util.jsonMapper
import no.nav.cv.eures.esco.dto.EscoDTO
import org.slf4j.LoggerFactory
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
        private val objectMapper = jsonMapper().rebuild()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .defaultTimeZone(TimeZone.getTimeZone("Europe/Oslo"))
            .build()
        val log = LoggerFactory.getLogger(OntologiClient::class.java)
    }

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .version(HttpClient.Version.HTTP_1_1)
        .build()

    fun hentEscoInformasjonFraOntologien(konseptId: String): EscoDTO? {
        if (konseptId.isBlank()) {
            log.warn("Prøvde å hente escoinformasjon for tom konseptId, returnerer null")
            return null
        }

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
