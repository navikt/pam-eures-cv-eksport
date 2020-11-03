package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper

// 4.7
data class CandidatePerson(
        val personName: Name,

        @JacksonXmlElementWrapper(useWrapping = false)
        val communication: List<Communication>,
        val residencyCountryCode: CountryCodeISO3166_Alpha_2,

        @JacksonXmlElementWrapper(useWrapping = false)
        val nationalityCode: List<CountryCodeISO3166_Alpha_2>,
        val birthDate: String,
        val genderCode: GenderCode,

        @JacksonXmlElementWrapper(useWrapping = false)
        val primaryLanguageCode: List<String>
)