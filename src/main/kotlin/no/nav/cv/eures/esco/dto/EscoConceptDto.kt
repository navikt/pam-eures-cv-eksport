package no.nav.cv.eures.esco.dto

import no.nav.cv.eures.model.JobCategoryCode

data class EscoConceptDto(val label: String, val kode: String, val type: EscoKodeType) {
    fun tilJobCategoryCode() = when (type) {
        EscoKodeType.ESCO -> JobCategoryCode(name = label, code = kode)
        EscoKodeType.ISCO -> JobCategoryCode(
            listName = "ISCO2008",
            listURI = "http://ec.europa.eu/esco/ConceptScheme/ISCO2008",
            listSchemeURI = "http://ec.europa.eu/esco/ConceptScheme/ISCO2008",
            listVersionID = "2008",
            name = label,
            code = kode
        )
    }
}

enum class EscoKodeType { ESCO, ISCO }
