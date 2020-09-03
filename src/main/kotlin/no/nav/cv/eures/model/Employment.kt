package no.nav.cv.eures.model

import no.nav.cv.eures.samtykke.Samtykke
import org.apache.avro.generic.GenericRecord

class Employment (
        private val cv: GenericRecord,
        private val samtykke: Samtykke
){
    fun getEmploymentHistory() : EmploymentHistory {
        if(!samtykke.utdanning) // TODO Ikke utdanning her
            return EmploymentHistory(listOf())

        return EmploymentHistory(listOf())
    }
}

data class EmploymentHistory(
        val employerHistory: List<EmployerHistory>
)

data class EmployerHistory(
    val organizationName: String,
    val organizationContact: PersonContact, // TODO Usikker paa denne mappingen
    val industryCode: IndustryCode,
    val employmentPeriod: AttendancePeriod
)

data class IndustryCode(val value: String)