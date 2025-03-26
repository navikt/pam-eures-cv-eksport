package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

enum class PositionOfferingTypes { DirectHire, Temporary, TemporaryToHire, ContractToHire, Contract, Internship, Apprenticeship, Seasonal, OnCall, RecruitmentReserve, SelfEmployed, Volunteer}

data class PositionOfferingTypeCode(
    @JacksonXmlProperty(isAttribute = true, localName = "listName")
    val listName: String = "PositionOfferingTypeCodeContentType",

    @JacksonXmlProperty(isAttribute = true, localName = "listURI")
    val listURI: String = "http://www.hr-xml.org/",

    @JacksonXmlProperty(isAttribute = true, localName = "listSchemeURI")
    val listSchemeURI: String = "http://www.hr-xml.org/3 ../Developer/Common/CodeLists.xsd",

    @JacksonXmlProperty(isAttribute = true, localName = "listVersionID")
    val listVersionID: String = "1.0",

    @JacksonXmlProperty(isAttribute = true, localName = "name")
    val name: String,

    @JacksonXmlText
    val code: String
)
