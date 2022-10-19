package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Arbeidserfaring
import no.nav.arbeid.cv.avro.Cv
import no.nav.cv.eures.janzz.JanzzService
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

    fun List<Arbeidserfaring>.toEmploymentList() = map {
        EmployerHistory(
                organizationName = it.arbeidsgiver ?: "",
                employmentPeriod = AttendancePeriod(
                        it.fraTidspunkt?.toFormattedDateTime() ?: DateText("Unknown"),
                        it.tilTidspunkt?.toFormattedDateTime()
                ),
                positionHistory = it.toPositionHistory())
    }

    fun Arbeidserfaring.toPositionHistory() = listOf(PositionHistory(
            positionTitle = stillingstittel ?: stillingstittelFritekst, // TODO Skal dette være friktekstfeltet?
            employmentPeriod = AttendancePeriod(
                    fraTidspunkt?.toFormattedDateTime() ?: DateText("Unknown"),
                    tilTidspunkt?.toFormattedDateTime()
            ),
            jobCategoryCode = stillingstittel?.toJobCategoryCode()
    ))

    private fun String.toJobCategoryCode(): JobCategoryCode? = janzzService.getEscoForTerm(this, JanzzService.EscoLookupType.OCCUPATION)
            .firstOrNull() // TODO Might consider something more refined than just picking the first result
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