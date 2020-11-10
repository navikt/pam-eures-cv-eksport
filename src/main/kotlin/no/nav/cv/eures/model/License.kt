package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper

data class Licenses(
        @JacksonXmlElementWrapper(useWrapping = false)
        val license: List<License>
)

data class License(
        val licenseTypeCode: String,
        val licenseName: String,
        val freeFormPeriod: FreeFormPeriod
)

data class FreeFormPeriod(
        val startDate: FormattedDateTime? = null,
        val endDate: FormattedDateTime? = null
)