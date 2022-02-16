package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import no.nav.arbeid.cv.avro.*
import no.nav.cv.eures.konverterer.toFormattedDateTime

data class Certifications(
        @JacksonXmlElementWrapper(useWrapping = false)
        val certification: List<Certification>
) {
    fun nullIfNoCerts() = if (certification.isEmpty()) null else this
}

data class Certification(
        val certificationTypeCode: String? = null, // TODO: Find out what certificationTypeCode should be
        val certificationName: String,
        val issuingAuthortity: IssuingAuthority,
        val firstIssuedDate: FormattedDateTime?,
        val freeFormEffectivePeriod: FreeFormEffectivePeriod?
) {
    constructor(godkjenning: Godkjenning): this(
        certificationName = godkjenning.tittel,
        issuingAuthortity = IssuingAuthority(godkjenning.utsteder),
        firstIssuedDate = godkjenning.gjennomfoert?.toFormattedDateTime(),
        freeFormEffectivePeriod = FreeFormEffectivePeriod(
            startDate = null,
            endDate = godkjenning.utloeper?.toFormattedDateTime()
        )
    )

    constructor(sertifikat: Sertifikat): this(
        certificationName = sertifikat.sertifikatnavn ?: sertifikat.sertifikatnavnFritekst,
        issuingAuthortity = IssuingAuthority(sertifikat.utsteder),
        firstIssuedDate = sertifikat.gjennomfoert?.toFormattedDateTime(),
        freeFormEffectivePeriod = FreeFormEffectivePeriod(
            startDate = null,
            endDate = sertifikat.utloeper?.toFormattedDateTime()
        )
    )

    constructor(kurs: Kurs): this(
        certificationName = kurs.tittel,
        issuingAuthortity = IssuingAuthority(kurs.utsteder),
        firstIssuedDate = kurs.tidspunkt?.toFormattedDateTime(),
        freeFormEffectivePeriod = null
    )

    constructor(fagdokumentasjon: Fagdokumentasjon): this(
        certificationName = fagdokumentasjon.tittel,
        issuingAuthortity = if(fagdokumentasjon.type == FagdokumentasjonType.AUTORISASJON) { IssuingAuthority("") }
                            else { IssuingAuthority("Yrkesopplæringsnemnd") },
        firstIssuedDate = null,
        freeFormEffectivePeriod = null
    )
}

data class IssuingAuthority(
        val name: String
)

data class FreeFormEffectivePeriod(
        val startDate: FormattedDateTime? = null,
        val endDate: FormattedDateTime? = null
)