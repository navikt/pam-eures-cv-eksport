package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import no.nav.arbeid.cv.avro.FoererkortKlasse
import no.nav.cv.eures.konverterer.toFormattedDateTime

data class Licenses(
        @JacksonXmlElementWrapper(useWrapping = false)
        val license: List<License>
)

data class License(
        val licenseTypeCode: String,
        val licenseName: String,
        val freeFormPeriod: FreeFormPeriod
) {
    constructor(foererkortKlasse: FoererkortKlasse): this(
        licenseTypeCode = foererkortKlasse.klasse,
        licenseName = foererkortKlasse.klasseBeskrivelse,
        freeFormPeriod = FreeFormPeriod(
            startDate = foererkortKlasse.fraTidspunkt?.toFormattedDateTime(),
            endDate = foererkortKlasse.utloeper?.toFormattedDateTime()
        )
    )
}

data class FreeFormPeriod(
        val startDate: FormattedDateTime? = null,
        val endDate: FormattedDateTime? = null
)