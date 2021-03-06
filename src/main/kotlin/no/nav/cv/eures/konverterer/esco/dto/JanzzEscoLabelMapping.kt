package no.nav.cv.eures.konverterer.esco.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class JanzzEscoLabelMapping (
        @JsonProperty("classifications")
        var classifications: JanzzEscoLabelMappingClassification,

        @JsonProperty("concept_id")
        var conceptId: Int,

        @JsonProperty("label")
        var label: String

)

data class JanzzEscoLabelMappingClassification(
        @JsonProperty("ESCO")
        var ESCO: Array<String>
)