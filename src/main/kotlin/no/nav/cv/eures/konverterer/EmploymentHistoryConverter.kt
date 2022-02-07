package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Arbeidserfaring
import no.nav.arbeid.cv.avro.Cv
import no.nav.cv.eures.janzz.JanzzService
import no.nav.cv.eures.model.*
import no.nav.cv.eures.samtykke.Samtykke
import org.springframework.stereotype.Service

@Service
class EmploymentHistoryConverter(
        private val janzzService: JanzzService
) {
    private val ikkeSamtykket = null

    fun toXmlRepresentation(cv: Cv, samtykke: Samtykke) = when (samtykke.arbeidserfaring) {
        true -> EmploymentHistory(cv.arbeidserfaring.toEmploymentList())
        false -> ikkeSamtykket
    }

    // TODO støtte null i fra-tidpsunkt
    private fun List<Arbeidserfaring>.toEmploymentList() = map {
        EmployerHistory(
                organizationName = it.arbeidsgiver ?: "",
                employmentPeriod = AttendancePeriod(
                        it.fraTidspunkt.toFormattedDateTime(),
                        it.tilTidspunkt?.toFormattedDateTime()
                ),
                positionHistory = it.toPositionHistory())
    }

    // TODO støtte null i fra-tidpsunkt
    private fun Arbeidserfaring.toPositionHistory() = listOf(PositionHistory(
            positionTitle = stillingstittel ?: stillingstittelFritekst, // TODO Skal dette være friktekstfeltet?
            employmentPeriod = AttendancePeriod(
                    fraTidspunkt.toFormattedDateTime(),
                    tilTidspunkt?.toFormattedDateTime()
            ),
            jobCategoryCode = stillingstittel?.toJobCategoryCode()
    ))

    private fun String.toJobCategoryCode(): JobCategoryCode? = janzzService.getEscoForOccupation(this)
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