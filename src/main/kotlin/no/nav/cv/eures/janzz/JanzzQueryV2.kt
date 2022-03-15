package no.nav.cv.eures.janzz

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.cv.eures.janzz.dto.JanzzEscoLabelMapping
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient

internal class JanzzQueryV2(
    private val branch: String,

) {
    companion object {
        private val objectMapper = ObjectMapper().registerModule(KotlinModule())
        private const val OCCUPATION_BRANCH = "occupation"
        private const val SKILL_BRANCH = "skill"
        val OCCUPATION_QUERY = JanzzQueryV2(OCCUPATION_BRANCH)
        val SKILL_QUERY = JanzzQueryV2(SKILL_BRANCH)
    }
    @Value("\${janzz.labels.host}") private val baseUrl: String = ""
    @Value("\${janzz.authorization.token}") private val token: String = ""
    private val authorization = "token $token"


    internal fun escoCodes(standardizedTerm: String) = EscoCode(escoClassifications(standardizedTerm), branch)

    private fun escoClassifications(standardizedTerm: String) = results(standardizedTerm)?.let {
        objectMapper.readValue<List<JanzzEscoLabelMapping>>(it).firstOrNull()?.classifications?.ESCO
    } ?: listOf()

    private fun results(
        query: String,
        lang: String = "no",
        output_classifications: String = "ESCO"
    ): String? =
        webClient().get()
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


    private fun webClient() =
        WebClient
            .builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, authorization)
            .build()
}