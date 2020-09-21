package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Utdannelse
import no.nav.cv.eures.model.*
import no.nav.cv.eures.samtykke.Samtykke



class EducationHistoryConverter(
        private val utdannelser: List<Utdannelse> = listOf(),
        private val samtykke: Samtykke
) {
    private val ikkeSamtykket = null

    fun toXmlRepresentation()
            = when(samtykke.utdanning) {
        true -> EducationHistory(utdannelser.toEducationList())
        false -> ikkeSamtykket
    }

    private fun List<Utdannelse>.toEducationList()
            = map { EducationOrganizationAttendance(
            organizationName = it.laerested,
            programName = it.utdanningsretning,
            attendancePeriod = AttendancePeriod(
                    it.fraTidspunkt.toFormattedDateTime(),
                    it.tilTidspunkt.toFormattedDateTime()),
            educationLevelCode = EducationLevelCode(code = it.nuskodeGrad.substring(0, 1))
    ) }
}
