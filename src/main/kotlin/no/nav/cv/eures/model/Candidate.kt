package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

@JacksonXmlRootElement
data class Candidate(
//        @JacksonXmlProperty(isAttribute = true, localName = "xmlns")
//        val xmlns: String = "http://www.hr-xml.org/3",
//
//        @JacksonXmlProperty(isAttribute = true, localName = "xmlns:oa")
//        val xmlns_oa: String = "http://www.openapplications.org/oagis/9",
//
        @JacksonXmlProperty(localName = "DocumentID")
        val documentId: DocumentId,
////        val created: String,
////        val createdBy: String?,
////        val updated: String,
////        val updatedBy: String?,
//
//        @JacksonXmlProperty(isAttribute = true, localName = "majorVersionID")
//        val majorVersionID: Int = 3,
//
//        @JacksonXmlProperty(isAttribute = true, localName = "minorVersionID")
//        val minorVersionID: Int = 2,
//
//        @JacksonXmlProperty(isAttribute = true, localName = "validFrom")
//        val validFrom: String,
//
//        @JacksonXmlProperty(isAttribute = true, localName = "validTo")
//        val validTo: String,
//
        @JacksonXmlElementWrapper(useWrapping = false)
        val candidateSupplier: List<CandidateSupplier>,
        val candidatePerson: CandidatePerson,
//        //val posistionSeekingStatus: PositionSeekingStatus?,
        val candidateProfile: CandidateProfile
)

data class DocumentId(
        @JacksonXmlProperty(isAttribute = true, localName = "schemeID")
        val schemeID: String = "NAV-002",
        @JacksonXmlProperty(isAttribute = true, localName = "schemeAgencyID")
        val schemeAgencyID: String = "NAV",
        @JacksonXmlProperty(isAttribute = true, localName = "schemeAgencyName")
        val schemeAgencyName: String = "NAV public employment services",
        @JacksonXmlProperty(isAttribute = true, localName = "schemeVersionID")
        val schemeVersionID: String = "1.3",
        @JacksonXmlText
        val uuid: String
)


// 4.28.5
enum class CountryCodeISO3166_Alpha_2 {
    NO
}

// 4.28.14
enum class GenderCode {
    NotKnown,
    Male,
    Female,
    NotSpecified
}

// 4.28.17
enum class LanguageCodeISO639_1_2002_Aplpha2 {
    NB
}

// 4.28.28
enum class PositionSeekingStatus {
    Active,
    Passive,
    NotConsideringPositions
}



