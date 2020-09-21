package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Arbeidserfaring
import no.nav.arbeid.cv.avro.Cv
import no.nav.cv.eures.model.*
import no.nav.cv.eures.samtykke.Samtykke

class EmploymentHistoryConverter (
        private val cv: Cv,
        private val samtykke: Samtykke
){
    private val ikkeSamtykket = null

    fun toXmlRepresentation()
            = when(samtykke.arbeidserfaring) {
        true -> EmploymentHistory(cv.arbeidserfaring.toEmploymentList())
        false -> ikkeSamtykket
    }

    fun List<Arbeidserfaring>.toEmploymentList()
            = map {
        EmployerHistory(
                organizationName = it.arbeidsgiver,
                employmentPeriod = AttendancePeriod(
                        it.fraTidspunkt.toFormattedDateTime(),
                        it.tilTidspunkt.toFormattedDateTime()
                ),
                industryCode = IndustryCode(it.janzzKonseptid))
    }

}

//val organizationName: String,
//val organizationContact: PersonContact, // TODO Usikker paa denne mappingen
//val industryCode: IndustryCode,
//val employmentPeriod: AttendancePeriod