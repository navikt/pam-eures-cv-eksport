package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper

data class EmploymentHistory(
        @JacksonXmlElementWrapper(useWrapping = false)
        val employerHistory: List<EmployerHistory>
)

data class EmployerHistory(
    val organizationName: String,
    //val organizationContact: PersonContact, // TODO Usikker paa denne mappingen
    val employmentPeriod: AttendancePeriod,
    val industryCode: IndustryCode // TODO Mapping Styke&Janzz
)

data class IndustryCode(val value: String)