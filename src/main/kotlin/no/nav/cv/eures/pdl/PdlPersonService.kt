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
            query = PdlHentStatsborgerskapQuery(ident = ident)
        )?.toStatsborgerskap()

        return statsborgerskap?.any { getEuresApprovedCountries().contains(it.land)
                && it.gyldigTilOgMed?.let{ LocalDate.parse(it).isAfter(LocalDate.now()) } ?: true}
    }

    override fun getIdenterUtenforEUSomHarSamtykket(identer: List<String>) : List<String>? {
        val identerUtenforEU = mutableListOf<HentPersonBolkDto.HentPersonData>()

        identer.chunked(1000).forEach{
            log.info("Chunk størrelse mot pdl ${it.size}")

            val samtykkeBrukere = hentStatsborgerskapForFlereFraPdl(
                query = PdlHentStatsborgerskapListeQuery(identer = it))

            identerUtenforEU.addAll(
                samtykkeBrukere?.personer?.filter { bruker ->
                    bruker.person?.statsborgerskap?.all { borgerskap -> !getEuresApprovedCountries().contains(borgerskap.land)
                            || (getEuresApprovedCountries().contains(borgerskap.land) && borgerskap.gyldigTilOgMed?.let{ LocalDate.parse(it).isBefore(LocalDate.now()) } ?: false)}
                        ?: false
                } ?: emptyList())

            log.info("Antall feil i kall mot PDL: ${samtykkeBrukere?.errors?.size} og antall tilsynelatende ok: ${samtykkeBrukere?.data?.hentPersonBolk?.size}" +
                    " og antall identerUtenforEU: ${identerUtenforEU.size}")
        }

        val countByCountry = identerUtenforEU
            .filter { it.person?.statsborgerskap != null }
            .flatMap { it.person!!.statsborgerskap!! }
            .groupingBy { it.land }
            .eachCount()
            .toList()
            .sortedWith(compareByDescending<Pair<String?, Int>> { it.second }.thenBy { it.first })
            .toMap()

        log.debug("Antall pr land: $countByCountry")

        return identerUtenforEU.filter{it.ident != null}.map{it.ident!!}
    }

    private fun getEuresApprovedCountries(): List<String> {
        return EuresCountries.values().map { it.toString() }
    }

    fun hentPersondataFraPdl(
        query: PdlQuery
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
            if (responseCode >= 300 || responseBody == null) {
                log.error("Fikk feil fra pdl: $responseBody")
                throw RuntimeException("unknown error (responseCode=$responseCode) from pdl")
            }

            val response = objectMapper.readValue(responseBody, HentPersonDto::class.java)
            if (response.errors?.firstOrNull() != null) {
                return null
            }

            return response
        } catch (ex: Exception) {
            log.error("Kall til PDL feilet", ex)
            return null
        }
    }

    fun hentStatsborgerskapForFlereFraPdl(
        query: PdlQueryMultiple
    ): HentPersonBolkDto? {
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
            if (responseCode >= 300 || responseBody == null) {
                log.error("Fikk feil fra pdl: $responseBody")
                throw RuntimeException("unknown error (responseCode=$responseCode) from pdl")
            }

            val response = objectMapper.readValue(responseBody, HentPersonBolkDto::class.java)
            if (response.errors?.firstOrNull() != null) {
                log.warn("Fikk error fra PDL ${response.errors.firstOrNull()?.message}")
            }

            return response
        } catch (ex: Exception) {
            log.error("Kall til PDL feilet", ex)
            return null
        }
    }

    enum class EuresCountries {
        AUT, BEL, BGR, HRV, CYP, CZE, DNK, EST, FIN, FRA, DEU, GRC, HUN, IRL, ITA, LVA, LTU, LUX, MLT, NLD, POL, PRT, ROU, SVK, SVN, ESP, SWE,
        CHE, LIE, NOR, ISL;
    }
}
