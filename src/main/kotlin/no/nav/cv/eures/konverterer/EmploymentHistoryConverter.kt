package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Arbeidserfaring
import no.nav.arbeid.cv.avro.Cv
import no.nav.cv.eures.konverterer.esco.JanzzService
import no.nav.cv.eures.model.*
import no.nav.cv.eures.samtykke.Samtykke

class EmploymentHistoryConverter(
        private val cv: Cv,
        private val samtykke: Samtykke,
        private val janzzService: JanzzService = JanzzService.instance()
) {
    private val ikkeSamtykket = null

    fun toXmlRepresentation() = when (samtykke.arbeidserfaring) {
        true -> EmploymentHistory(cv.arbeidserfaring.toEmploymentList())
        false -> ikkeSamtykket
    }

    // TODO støtte null i fra-tidpsunkt
    fun List<Arbeidserfaring>.toEmploymentList() = map {
        EmployerHistory(
                organizationName = it?.arbeidsgiver ?: "",
                employmentPeriod = AttendancePeriod(
                        it.fraTidspunkt.toFormattedDateTime(),
                        it.tilTidspunkt?.toFormattedDateTime()
                ),
                positionHistory = it.toPositionHistory())
    }

    // TODO støtte null i fra-tidpsunkt
    fun Arbeidserfaring.toPositionHistory() = listOf(PositionHistory(
            positionTitle = stillingstittel ?: stillingstittelFritekst, // TODO Skal dette være friktekstfeltet?
            employmentPeriod = AttendancePeriod(
                    fraTidspunkt.toFormattedDateTime(),
                    tilTidspunkt?.toFormattedDateTime()
            ),
            jobCategoryCode = janzzKonseptid.toJobCategoryCode()
    ))

    private fun String.toJobCategoryCode(): JobCategoryCode? = janzzService.getEscoForConceptId(this)
            .firstOrNull()
            ?.let {
                JobCategoryCode(
                        name = it.term,
                        code = it.esco
                )
            }



}


//val organizationName: String,
//val organizationContact: PersonContact, // TODO Usikker paa denne mappingen
//val industryCode: IndustryCode,
//val employmentPeriod: AttendancePeriod