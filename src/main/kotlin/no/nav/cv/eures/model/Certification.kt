package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Certifications(
        @JacksonXmlElementWrapper(useWrapping = false)
        val certification: List<Certification>
)

data class Certification(
        val certificationTypeCode: String?,
        val certificationName: String,
        val issuingAuthority: IssuingAuthority,
        val firstIssuedDate: FormattedDateTime?,
        val freeFormEffectivePeriod: FreeFormEffectivePeriod?,
)

data class IssuingAuthority(
        @JacksonXmlProperty(localName = "oa:Name")
        val name: String
)

data class FreeFormEffectivePeriod(
        val startDate: FormattedDateTime? = null,
        val endDate: FormattedDateTime? = null
)