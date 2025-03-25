package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

enum class PositionSchedule { FullTime, PartTime, FlexTime, Any }

data class PositionScheduleTypeCode(
    @JacksonXmlProperty(isAttribute = true, localName = "listName")
    val listName: String = "PositionScheduleCodeContentType",

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
