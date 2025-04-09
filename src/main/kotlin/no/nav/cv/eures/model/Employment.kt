package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

data class EmploymentHistory(
        @JacksonXmlElementWrapper(useWrapping = false)
        val employerHistory: List<EmployerHistory>
)

data class EmployerHistory(
        val organizationName: String,
        //val industryCode: IndustryCode, // TODO Denne har vi ikke mulighet til å vite
        //val organizationContact: PersonContact, // TODO Usikker paa denne mappingen
        val employmentPeriod: AttendancePeriod,

        @JacksonXmlElementWrapper(useWrapping = false)
        val positionHistory: List<PositionHistory>



)

data class IndustryCode(
        @JacksonXmlText
        val code: String
)

data class PositionHistory(
        val positionTitle: String,
        val employmentPeriod: AttendancePeriod,
        val jobCategoryCode: JobCategoryCode?
)

data class JobCategoryCode(
        @JacksonXmlProperty(isAttribute = true, localName = "listName")
        val listName: String = "ESCO_Occupations",

        @JacksonXmlProperty(isAttribute = true, localName = "listURI")
        val listURI: String = "https://ec.europa.eu/esco/portal",

        @JacksonXmlProperty(isAttribute = true, localName = "listSchemeURI")
        val listSchemeURI: String ="https://ec.europa.eu/esco/portal",

        @JacksonXmlProperty(isAttribute = true, localName = "listVersionID")
        val listVersionID: String = "ESCOv1",

        @JacksonXmlProperty(isAttribute = true, localName = "name")
        val name: String,

        //@JacksonXmlProperty(localName = "text")
        @JacksonXmlText
        val code: String
)
