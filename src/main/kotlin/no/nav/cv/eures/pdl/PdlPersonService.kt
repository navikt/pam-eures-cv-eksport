package no.nav.cv.eures.pdl

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.function.Supplier

@Service
@Profile("!dev & !test")
class PdlPersonService(
    @Qualifier("pdlTokenProvider") private val tokenProvider: Supplier<String>,
) : PdlPersonGateway {
    companion object {
        private val log = LoggerFactory.getLogger(PdlPersonService::class.java)
        private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
    }

    private val url: String = "${System.getenv("PDL_BASE_URL") ?: "https://pdl-api.dev.intern.nav.no"}/graphql"

    override fun erEUEOSstatsborger(ident: String): Boolean? {
        val statsborgerskap = hentPersondataFraPdl(
            ident = ident,
            query = PdlHentStatsborgerskapQuery(ident = ident)
        )?.toStatsborgerskap()

        return statsborgerskap?.any { getEuresApprovedCountries().contains(it.land)
                && it.gyldigTilOgMed?.let{ LocalDate.parse(it).isAfter(LocalDate.now()) } ?: true}
    }

    private fun getEuresApprovedCountries(): List<String> {
        return EuresCountries.values().map { it.toString() }
    }

    fun hentPersondataFraPdl(
        ident: String,
        query: PdlQuery = PdlHentStatsborgerskapQuery(ident = ident)
    ): HentPersonDto? {
        try {
            log.info("Henter persondata fra PDL")

            val (responseCode, responseBody) = with(URL(url).openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true

                setRequestProperty("Authorization", "Bearer ${tokenProvider.get()}")
                setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                setRequestProperty("Tema", "REK")

                outputStream.writer(Charsets.UTF_8).apply {
                    write(objectMapper.writeValueAsString(query))
                    flush()
                }

                val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
                responseCode to stream?.use { s -> s.bufferedReader().readText() }
            }
            // Vi må kunne skille på Not Found og feil. Hva returneres hvis personen ikke er i KRR?
            if (responseCode >= 300 || responseBody == null) {
                log.error("Fikk feil fra pdl: $responseBody")
                throw RuntimeException("unknown error (responseCode=$responseCode) from pdl")
            }

            val response = objectMapper.readValue(responseBody, HentPersonDto::class.java)

            val error = response.errors?.firstOrNull()

            if (error?.extensions?.get("code") == "not_found") {
                return null
            } else if (error != null) {
                throw Exception(error.message)
            }

            return response
        } catch (ex: Exception) {
            log.error("Kall til PDL feilet", ex)
            return null
        }
    }

    enum class EuresCountries {
        AUT, BEL, BGR, HRV, CYP, CZE, DNK, EST, FIN, FRA, DEU, GRC, HUN, IRL, ITA, LVA, LTU, LUX, MLT, NLD, POL, PRT, ROU, SVK, SVN, ESP, SWE,
        GBR, CHE, LIE, NOR, ISL;
    }
}