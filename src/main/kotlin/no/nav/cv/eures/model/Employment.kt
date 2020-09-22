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
        val employmentPeriod: AttendancePeriod
        //val jobCategoryCode: JobCategoryCode // TODO Denne har vi ikke mulighet til å vite
)

data class JobCategoryCode(
        @JacksonXmlProperty(isAttribute = true, localName = "listName")
        val listName: String = "ISCO2008",

        @JacksonXmlProperty(isAttribute = true, localName = "listURI")
        val listURI: String = "http://ec.europa.eu/esco/ConceptScheme/ISCO2008",

        @JacksonXmlProperty(isAttribute = true, localName = "listVersionID")
        val listVersionID: String = "2008",

        @JacksonXmlProperty(isAttribute = true, localName = "name")
        val name: String = "Stillingsnavn", // TODO Fix

        @JacksonXmlText
        val code: String
)