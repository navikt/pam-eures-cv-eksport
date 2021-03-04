package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper

// 4.7
data class CandidatePerson(
        val personName: Name,

        @JacksonXmlElementWrapper(useWrapping = false)
        val communication: List<Communication>,
        val residencyCountryCode: String?,

        @JacksonXmlElementWrapper(useWrapping = false)
        val nationalityCode: List<String>?,
        val birthDate: String,
        val genderCode: GenderCode,

        @JacksonXmlElementWrapper(useWrapping = false)
        val primaryLanguageCode: List<String>
)