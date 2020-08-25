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

// 4.6
data class PersonContact(
        val personName: Name,
        val communication: List<Communication>

)

// 4.6.3 and 4.8
data class Name(
        val givenName: String,
        val familyName: String
)

// 4.6.4 and 4.9
data class Communication(
        val channelCode: ChannelCode,
        val choice: Choice
)

// 4.28.3
enum class ChannelCode{
        Telephone,
        MobileTelephone,
        Fax,
        Email,
        InstantMessage,
        Web
}

// 4.6.5 and 4.9.3
data class Choice(
        val address: Address?,
        val dialNumber: String?,
        val URI: String?
)

// 4.9.4
data class Address(
        val cityName: String,
        val countryCode: CountryCodeISO3166_Alpha_2,
        val postalCode: PostalCode
)

// EURES_PostalCodes.gc  NUTS 2013
enum class PostalCode {}

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
        val educationHistory: EducationHistory
)

// 4.13
data class EducationHistory(
        val organizationAttendance: List<OrganizationAttendance>
)

// 4.13.3
data class OrganizationAttendance(
        val organizationName: String,
        val educationLevel: EducationLevel,
        val attendancePeriod: AttendancePeriod
)

// 4.28.12
enum class EducationLevel(code: Int) {
    EarlyChildhood(0),
    Primary(1),
    LowerSecondary(2),
    UpperSecondary(3),
    PostSecondaryNonTertiary(4),
    ShortCycleTertiary(5),
    Bachelor(6),
    Masters(7),
    Doctoral(8)
}

// 4.13.7.2
data class AttendancePeriod(
        val startDate: String,
        val endDate: String?
)