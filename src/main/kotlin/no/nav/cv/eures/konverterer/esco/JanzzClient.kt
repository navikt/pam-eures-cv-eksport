package no.nav.cv.eures.konverterer.esco

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class JanzzClient(
        @Value("\${janzz.labels.host}") private val baseUrl: String
) {
    private val log: Logger = LoggerFactory.getLogger(JanzzClient::class.java)

    fun search(
            authorization: String,
            query: String,
            limit: String,
            lang: String = "no",
            branch: String = "skill",
            CLASS_ESCO: String = "*",
            output_classifications: String = "ESCO"
    ): String? {
        val client = WebClient
                .builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorization)
                .build()

        return client.get()
                .uri { uriBuilder ->
                    uriBuilder
                            .path("/japi/labels/")
                            .queryParam("q", query)
                            .queryParam("limit", limit)
                            .queryParam("lang", lang)
                            .queryParam("branch", branch)
                            .queryParam("CLASS_ESCO", CLASS_ESCO)
                            .queryParam("output_classifications", output_classifications)
                            .build()
                }
                .retrieve()
                .bodyToMono(String::class.java)
                .block()
    }

    fun lookupConceptId (
            authorization: String,
            conceptId: String,
            lang: String = "no",
            CLASS_ESCO: String = "*",
            output_classifications: String = "ESCO"
    ): String? {
        val client = WebClient
                .builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorization)
                .build()

        return client.get()
                .uri { uriBuilder ->
                    uriBuilder
                            .path("/japi/concepts/$conceptId")
                            .queryParam("lang", lang)
                            .queryParam("CLASS_ESCO", CLASS_ESCO)
                            .queryParam("output_classifications", output_classifications)
                            .build()
                }
                .retrieve()
                .bodyToMono(String::class.java)
                .block()
    }
}
