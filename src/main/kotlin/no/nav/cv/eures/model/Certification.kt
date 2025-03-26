package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper

data class Certifications(
        @JacksonXmlElementWrapper(useWrapping = false)
        val certification: List<Certification>
)

data class Certification(
        val certificationTypeCode: String?,
        val certificationName: String,
        val issuingAuthority: IssuingAuthority,
        val firstIssuedDate: FormattedDateTime?,
        val freeFormEffectivePeriod: FreeFormEffectivePeriod?
)

data class IssuingAuthority(
        val name: String
)

data class FreeFormEffectivePeriod(
        val startDate: FormattedDateTime? = null,
        val endDate: FormattedDateTime? = null
)