package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.*
import no.nav.cv.eures.model.Certification
import no.nav.cv.eures.model.Certifications
import no.nav.cv.eures.model.FreeFormEffectivePeriod
import no.nav.cv.eures.model.IssuingAuthority
import no.nav.cv.eures.samtykke.Samtykke

class CertificationConverter(
        private val cv: Cv,
        private val samtykke: Samtykke
) {
    private val ikkeSamtykket = null

    fun toXmlRepresentation(): Certifications? {
        val certs = mutableListOf<Certification>()

        if (samtykke.offentligeGodkjenninger)
            certs.addAll(cv.godkjenninger.toCertifications())

        if (samtykke.andreGodkjenninger)
            certs.addAll(cv.sertifikat.toCertifications())

        if (samtykke.kurs)
            certs.addAll(cv.kurs.toCertifications())

        if(samtykke.fagbrev)
            certs.addAll(cv.fagdokumentasjon.toCertifications())

        return if (certs.isEmpty()) null else Certifications(certs)
    }

    @JvmName("toCertificationsGodkjenning")
    private fun List<Godkjenning>.toCertifications() = mapNotNull {
        Certification(
                certificationTypeCode = null, // TODO: Find out what certificationTypeCode should be
                certificationName = it.tittel,
                issuingAuthortity = IssuingAuthority(it.utsteder),
                firstIssuedDate = it.gjennomfoert.toFormattedDateTime(),
                freeFormEffectivePeriod = FreeFormEffectivePeriod(
                        startDate = null,
                        endDate = it.utloeper.toFormattedDateTime()
                )
        )
    }

    @JvmName("toCertificationsSertifikat")
    private fun List<Sertifikat>.toCertifications() = mapNotNull {
        Certification(
                certificationTypeCode = null, // TODO: Find out what certificationTypeCode should be
                certificationName = it.sertifikatnavn ?: it.sertifikatnavnFritekst,
                issuingAuthortity = IssuingAuthority(it.utsteder),
                firstIssuedDate = it.gjennomfoert.toFormattedDateTime(),
                freeFormEffectivePeriod = FreeFormEffectivePeriod(
                        startDate = null,
                        endDate = it.utloeper.toFormattedDateTime()
                )
        )
    }

    @JvmName("toCertificationsKurs")
    private fun List<Kurs>.toCertifications() = mapNotNull {
        Certification(
                certificationTypeCode = null, // TODO: Find out what certificationTypeCode should be
                certificationName = it.tittel,
                issuingAuthortity = IssuingAuthority(it.utsteder),
                firstIssuedDate = it.tidspunkt.toFormattedDateTime(),
                freeFormEffectivePeriod = null
        )
    }

    @JvmName("toCertificationsFagdokumentasjon")
    private fun List<Fagdokumentasjon>.toCertifications() = mapNotNull {
        Certification(
                certificationTypeCode = null, // TODO: Find out what certificationTypeCode should be
                certificationName = it.tittel,
                issuingAuthortity = IssuingAuthority("Yrkesopplæringsnemnd"),
                firstIssuedDate = null,
                freeFormEffectivePeriod = null
        )
    }
}