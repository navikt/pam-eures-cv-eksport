package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper

data class CandidatePositionPreferences(
    @JacksonXmlElementWrapper(useWrapping = false)
    val preferredLocation: List<PreferredLocation>?,
    @JacksonXmlElementWrapper(useWrapping = false)
    val jobCategory: List<JobCategory>?,
    @JacksonXmlElementWrapper(useWrapping = false)
    val positionOfferingTypeCode: List<PositionOfferingTypeCode>?,
    @JacksonXmlElementWrapper(useWrapping = false)
    val positionScheduleTypeCode: List<PositionScheduleTypeCode>?
)

data class PreferredLocation(
    val referenceLocation: ReferenceLocation
)

data class ReferenceLocation(
    val countryCode: String
)

data class JobCategory(
    val jobCategoryCode: JobCategoryCode
)
