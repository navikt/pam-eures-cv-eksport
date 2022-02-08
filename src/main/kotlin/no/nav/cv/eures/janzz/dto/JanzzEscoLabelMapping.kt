package no.nav.cv.eures.janzz.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class JanzzEscoLabelMapping (
    @JsonProperty("classifications")
        var classifications: JanzzEscoMappingClassification,

    @JsonProperty("concept_id")
        var conceptId: Int,

    @JsonProperty("label")
        var label: String

)

data class JanzzEscoMappingClassification(
        @JsonProperty("ESCO")
        var ESCO: List<String>
)