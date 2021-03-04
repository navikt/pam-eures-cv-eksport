package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.*
import no.nav.cv.eures.model.Certification
import no.nav.cv.eures.model.Certifications
import no.nav.cv.eures.model.FreeFormEffectivePeriod
import no.nav.cv.eures.model.IssuingAuthority
import no.nav.cv.eures.samtykke.Samtykke
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CertificationConverter(
        private val cv: Cv,
        private val samtykke: Samtykke
) {
    private val ikkeSamtykket = null

    companion object {
        val log: Logger = LoggerFactory.getLogger(CertificationConverter::class.java)
    }

    fun toXmlRepresentation(): Certifications? {
        val certs = mutableListOf<Certification>()

        if (samtykke.offentligeGodkjenninger)
            certs.addAll(cv.godkjenninger.toCertifications())

        if (samtykke.andreGodkjenninger)
            certs.addAll(cv.sertifikat.toCertifications())

        if (samtykke.kurs)
            certs.addAll(cv.kurs.toCertifications())

        val (autorisasjon, fagbrev) = cv.fagdokumentasjon.toCertifications()

        if (samtykke.offentligeGodkjenninger)
            certs.addAll(autorisasjon)

        if (samtykke.fagbrev)
            certs.addAll(fagbrev)

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
                        endDate = it.utloeper?.toFormattedDateTime()
                )
        )
    }

    @JvmName("toCertificationsSertifikat")
    private fun List<Sertifikat>.toCertifications() = mapNotNull {
        Certification(
                certificationTypeCode = null, // TODO: Find out what certificationTypeCode should be
                certificationName = it.sertifikatnavn ?: it.sertifikatnavnFritekst,
                issuingAuthortity = IssuingAuthority(it.utsteder),
                firstIssuedDate = it.gjennomfoert?.toFormattedDateTime(),
                freeFormEffectivePeriod = FreeFormEffectivePeriod(
                        startDate = null,
                        endDate = it.utloeper?.toFormattedDateTime()
                )
        )
    }

    @JvmName("toCertificationsKurs")
    private fun List<Kurs>.toCertifications() = mapNotNull {
        Certification(
                certificationTypeCode = null, // TODO: Find out what certificationTypeCode should be
                certificationName = it.tittel,
                issuingAuthortity = IssuingAuthority(it.utsteder),
                firstIssuedDate = it.tidspunkt?.toFormattedDateTime(),
                freeFormEffectivePeriod = null
        )
    }

    @JvmName("toCertificationsFagdokumentasjon")
    private fun List<Fagdokumentasjon>.toCertifications() : Pair<List<Certification>, List<Certification>> {
        val (aut, fag) = partition { it.type == FagdokumentasjonType.AUTORISASJON }

        return Pair(
                aut.map {
                    Certification(
                            certificationTypeCode = null, // TODO: Find out what certificationTypeCode should be
                            certificationName = it.tittel,
                            issuingAuthortity = IssuingAuthority(""),
                            firstIssuedDate = null,
                            freeFormEffectivePeriod = null
                    )
                },
                fag.map {
                    Certification(
                            certificationTypeCode = null, // TODO: Find out what certificationTypeCode should be
                            certificationName = it.tittel,
                            issuingAuthortity = IssuingAuthority("Yrkesopplæringsnemnd"),
                            firstIssuedDate = null,
                            freeFormEffectivePeriod = null
                    )
                }
        )
    }
}