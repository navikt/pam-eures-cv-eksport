package no.nav.cv.eures.pdl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class HentPersonBolkDto(
    val data: HentPersonBolk? = null,
    val errors: List<PdlError>? = null
) {
    val personer get() = data?.hentPersonBolk

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PdlError(
        var message: String? = null,
        var locations: List<Map<String, Int>>? = listOf(),
        var path: List<String>? = listOf(),
        var extensions: Map<String, String>? = mapOf()
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class HentPersonBolk(
        val hentPersonBolk: List<HentPersonData>? = listOf()
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class HentPersonData(
        val ident: String? = null,
        val person: Person? = null
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Person(
            val statsborgerskap: List<Statsborgerskap>? = listOf()
        ) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            data class Statsborgerskap(
                val land: String?,
                val gyldigFraOgMed: String?,
                val gyldigTilOgMed: String?
            )
        }
    }
}

