package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Foererkort
import no.nav.cv.eures.model.FreeFormPeriod
import no.nav.cv.eures.model.License
import no.nav.cv.eures.model.Licenses
import no.nav.cv.eures.samtykke.Samtykke

class LicensesConverter (
        private val cv: Cv,
        private val samtykke: Samtykke
) {
    private val ikkeSamtykket = null

    fun toXmlRepresentation()
            = when(samtykke.foererkort) {
                true -> cv.foererkort.toLicenses()
                false -> ikkeSamtykket
    }

    fun Foererkort.toLicenses()
            = Licenses(klasse.map {
                License(
                        licenseTypeCode = it.klasse,
                        licenseName = it.klasseBeskrivelse,
                        freeFormPeriod = FreeFormPeriod(
                                startDate = it.fraTidspunkt?.toFormattedDateTime(),
                                endDate = it.utloeper?.toFormattedDateTime()
                        )
                ) })

}