package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Foererkort
import no.nav.cv.eures.model.FreeFormPeriod
import no.nav.cv.eures.model.License
import no.nav.cv.eures.model.Licenses
import no.nav.cv.eures.samtykke.Samtykke
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LicensesConverter (
        private val cv: Cv,
        private val samtykke: Samtykke
) {
    private val ikkeSamtykket = null

    companion object {
        val log: Logger = LoggerFactory.getLogger(CertificationConverter::class.java)
    }

    fun toXmlRepresentation()
            = when(samtykke.foererkort) {
                true -> cv.foererkort.toLicenses()
                false -> ikkeSamtykket
    }

    fun Foererkort.toLicenses()
            = Licenses(klasse.map {
                log.debug("FOERERKORT ${cv.aktoerId}: Mapping klasse ${it.klasse} and beskrivelse ${it.klasseBeskrivelse}")
                License(
                        licenseTypeCode = it.klasse,
                        licenseName = it.klasseBeskrivelse,
                        freeFormPeriod = FreeFormPeriod(
                                startDate = it.fraTidspunkt?.toFormattedDateTime(),
                                endDate = it.utloeper?.toFormattedDateTime()
                        )
                ) })

}