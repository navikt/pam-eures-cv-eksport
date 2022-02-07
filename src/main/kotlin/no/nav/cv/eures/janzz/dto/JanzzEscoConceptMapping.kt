package no.nav.cv.eures.janzz.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class JanzzEscoConceptMapping(
    @JsonProperty("id")
        var id: Int,

    @JsonProperty("preferred_label")
        var preferredLabel: String,

    @JsonProperty("classifications")
        var classifications: JanzzEscoMappingClassification,
)
