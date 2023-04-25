package no.nav.cv.eures.pdl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class HentPersonDto(
    val data: HentPersonData? = null,
    val errors: List<PdlError>? = null
) {

    val person get() = data?.hentPerson
    val ident get() = data?.ident

    data class PdlError(
        var message: String? = null,
        var locations: List<Map<String, Int>>? = listOf(),
        var path: List<String>? = listOf(),
        var extensions: Map<String, String>? = mapOf()
    )

    data class HentPersonData(
        val ident: String? = null,
        val hentPerson: Person? = null
    ) {
        data class Person(
            val statsborgerskap: List<Statsborgerskap>? = listOf()
        ) {
            data class Statsborgerskap(
                val land: String?,
                val gyldigFraOgMed: String?,
                val gyldigTilOgMed: String?
            )
        }
    }

    fun toStatsborgerskap(): List<HentPersonData.Person.Statsborgerskap>? {
        return data?.hentPerson?.statsborgerskap;
    }
}
