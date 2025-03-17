package no.nav.cv.eures.esco.dto

data class KonseptGrupperingDTO(
    val konseptId: Long,
    val noLabel: String?,
    val styrk08SSB: List<String>,
    val esco: EscoDTO?
)

data class EscoDTO(
    val label: String?,
    val uri: String
)
