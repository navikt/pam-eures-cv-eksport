package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.*
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.cv.CvEndretInternAuthorization
import no.nav.cv.dto.cv.CvEndretInternCertificate
import no.nav.cv.dto.cv.CvEndretInternCourse
import no.nav.cv.dto.cv.CvEndretInternVocationalCertificate
import no.nav.cv.eures.model.Certification
import no.nav.cv.eures.model.Certifications
import no.nav.cv.eures.model.FreeFormEffectivePeriod
import no.nav.cv.eures.model.IssuingAuthority
import no.nav.cv.eures.samtykke.Samtykke
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CertificationConverter2(
        private val dto: CvEndretInternDto,
        private val samtykke: Samtykke
) {
    private val ikkeSamtykket = null

    companion object {
        val log: Logger = LoggerFactory.getLogger(CertificationConverter2::class.java)
    }

    fun toXmlRepresentation(): Certifications? {
        val certs = mutableListOf<Certification>()


        val (autorisasjon, fagbrev) = dto.cv?.vocationalCertificates?.toCertifications() ?: Pair(emptyList(), emptyList())

        if (samtykke.offentligeGodkjenninger)
            certs.addAll(dto.cv?.authorizations?.toCertifications().orEmpty())
            certs.addAll(autorisasjon)


        if (samtykke.andreGodkjenninger)
            certs.addAll(dto.cv?.certificates?.toCertifications().orEmpty())

        if (samtykke.kurs)
            certs.addAll(dto.cv?.courses?.toCertifications().orEmpty())

        if (samtykke.fagbrev)
            certs.addAll(fagbrev)

        return if (certs.isEmpty()) ikkeSamtykket else Certifications(certs)
    }

    @JvmName("toCertificationsGodkjenning")
    private fun List<CvEndretInternAuthorization>.toCertifications() = mapNotNull {
        it.title ?: return@mapNotNull null

        Certification(
                certificationTypeCode = null,
                certificationName = it.title,
                issuingAuthortity = IssuingAuthority(it.issuer ?: ""),
                firstIssuedDate = it.fromDate?.toFormattedDateTime(),
                freeFormEffectivePeriod = FreeFormEffectivePeriod(
                        startDate = null,
                        endDate = it.toDate?.toFormattedDateTime()
                )
        )
    }

    @JvmName("toCertificationsSertifikat")
    private fun List<CvEndretInternCertificate>.toCertifications() = mapNotNull {
        it.certificateName ?: it.alternativeName ?: return@mapNotNull null

        Certification(
                certificationTypeCode = null,
                certificationName = it.certificateName ?: it.alternativeName ?: return@mapNotNull null,
                issuingAuthortity = IssuingAuthority(it.issuer ?: ""),
                firstIssuedDate = it.fromDate?.toFormattedDateTime(),
                freeFormEffectivePeriod = FreeFormEffectivePeriod(
                        startDate = null,
                        endDate = it.toDate?.toFormattedDateTime()
                )
        )
    }

    @JvmName("toCertificationsKurs")
    private fun List<CvEndretInternCourse>.toCertifications() = mapNotNull {
        it.title ?: return@mapNotNull null

        Certification(
                certificationTypeCode = null,
                certificationName = it.title.replace(CandidateProfileConverter.xml10Pattern, ""),
                issuingAuthortity = IssuingAuthority((it.issuer ?: "").replace(CandidateProfileConverter.xml10Pattern, "")),
                firstIssuedDate = it.date?.toFormattedDateTime(),
                freeFormEffectivePeriod = null
        )
    }

    @JvmName("toCertificationsFagdokumentasjon")
    private fun List<CvEndretInternVocationalCertificate>.toCertifications() : Pair<List<Certification>, List<Certification>> {
        val (aut, fag) = partition { it.certificateType == FagdokumentasjonType.AUTORISASJON.toString() }

        return Pair(
                aut.mapNotNull {
                    it.title ?: return@mapNotNull null
                    Certification(
                            certificationTypeCode = null, // TODO: Find out what certificationTypeCode should be
                            certificationName = it.title,
                            issuingAuthortity = IssuingAuthority(""),
                            firstIssuedDate = null,
                            freeFormEffectivePeriod = null
                    )
                },

                fag.mapNotNull {
                    it.title ?: return@mapNotNull null
                    Certification(
                            certificationTypeCode = null, // TODO: Find out what certificationTypeCode should be
                            certificationName = it.title,
                            issuingAuthortity = IssuingAuthority("Yrkesopplæringsnemnd"),
                            firstIssuedDate = null,
                            freeFormEffectivePeriod = null
                    )
                }
        )
    }
}