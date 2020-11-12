package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper

data class PersonQualifications(
        @JacksonXmlElementWrapper(useWrapping = false)
        val personCompetency: List<PersonCompetency>
)

data class PersonCompetency(
        val competencyID: String,
        val taxonomyID: String,
        val competencyName: String? = null,
        val proficiencyLevel: String? = null,

        @JacksonXmlElementWrapper(useWrapping = false)
        val competencyDimension: List<CompetencyDimension>? = null

)

data class  CompetencyDimension(
        val competencyDimensionTypeCode: String,
        val score: CompetencyDimensionScore
)

data class CompetencyDimensionScore(
        val scoreText: String
)