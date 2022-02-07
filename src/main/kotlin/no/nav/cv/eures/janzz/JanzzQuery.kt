package no.nav.cv.eures.janzz

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class JanzzQuery(
    @Value("\${janzz.labels.host}") private val baseUrl: String
) {
    fun janzzSearch(
        authorization: String,
        query: String,
        branchType: JanzzService.EscoLookupType,
        lang: String = "no",
        output_classifications: String = "ESCO"
    ): String? {
        val client = WebClient
            .builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, authorization)
            .build()

        val branch = if (branchType == JanzzService.EscoLookupType.SKILL) "skill" else "occupation"

        return client.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/japi/labels/")
                    .queryParam("q", query)
                    .queryParam("lang", lang)
                    .queryParam("branch", branch)
                    .queryParam("exact_match", true)
                    .queryParam("output_classifications", output_classifications)
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }
}