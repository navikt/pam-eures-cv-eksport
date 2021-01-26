package no.nav.cv.eures.konverterer.esco.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class JanzzEscoConceptMapping(
        @JsonProperty("id")
        var id: Int,

        @JsonProperty("preferred_label")
        var preferredLabel: String,

        @JsonProperty("classification_set")
        var classificationSet: List<JanzzEscoConceptClassSetMapping>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JanzzEscoConceptClassSetMapping(
        @JsonProperty("classification")
        var classification: String,

        @JsonProperty("val")
        var value: String
)