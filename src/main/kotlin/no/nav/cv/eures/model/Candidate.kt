package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty


data class Candidate(
        @JacksonXmlProperty(isAttribute = true, localName = "xmlns")
        val xmlns: String = "http://www.hr-xml.org/3",

        val id: Id,
        val uuid: String,
        val created: String,
        val createdBy: String?,
        val updated: String,
        val updatedBy: String?,

        @JacksonXmlProperty(isAttribute = true, localName = "majorVersionID")
        val majorVersionID: Int = 3,

        @JacksonXmlProperty(isAttribute = true, localName = "minorVersionID")
        val minorVersionID: Int = 2,

        @JacksonXmlProperty(isAttribute = true, localName = "validFrom")
        val validFrom: String,

        @JacksonXmlProperty(isAttribute = true, localName = "validTo")
        val validTo: String,

        @JacksonXmlElementWrapper(useWrapping = false)
        val candidateSupplier: List<CandidateSupplier>,
        val person: Person,
        val posistionSeekingStatus: PositionSeekingStatus?,
        val profile: Profile
)

data class Id(val documentId: String)

// 4.5
data class CandidateSupplier(
        val partyId: String,
        val partyName: String,

        @JacksonXmlElementWrapper(useWrapping = false)
        val personContact: List<PersonContact>,
        val precedence: Int
)


// 4.7
data class Person(
        val personName: Name,
        val communication: List<Communication>,
        val residencyCountryCode: CountryCodeISO3166_Alpha_2,
        val nationality: List<CountryCodeISO3166_Alpha_2>,
        val birthDate: String,
        val gender: GenderType,
        val primaryLanguageCode: List<LanguageCodeISO639_1_2002_Aplpha2>
)

// 4.28.5
enum class CountryCodeISO3166_Alpha_2 {
    NO
}

// 4.28.14
enum class GenderType {
    NotKnown,
    Male,
    Female,
    NotSpecified
}

// 4.28.17
enum class LanguageCodeISO639_1_2002_Aplpha2 {
    Norwegian
}

// 4.28.28
enum class PositionSeekingStatus {
    Active,
    Passive,
    NotConsideringPositions
}

// 4.11
data class Profile(
        val educationHistory: EducationHistory,
        val employmentHistory: EmploymentHistory
)

