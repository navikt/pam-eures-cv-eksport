package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.FagdokumentasjonType
import no.nav.arbeid.cv.avro.Jobbprofil
import no.nav.cv.eures.janzz.dto.CachedEscoMapping
import no.nav.cv.eures.model.dto.ArbeidserfaringWithEscoDto
import no.nav.cv.eures.samtykke.Samtykke

// 4.11
data class CandidateProfile(
    @JacksonXmlProperty(isAttribute = true, localName = "languageCode")
    val languageCode: String = "no",
    val executiveSummary: String = "",
    val employmentHistory: EmploymentHistory? = null,
    val educationHistory: EducationHistory? = null,
    val licenses: Licenses? = null,
    val certifications: Certifications? = null,
    val personQualifications: PersonQualifications? = null
) {
    constructor(
        samtykke: Samtykke,
        cv: Cv,
        jobbProfil: Jobbprofil,
        arbeidserfaringerWithEsco: List<ArbeidserfaringWithEscoDto>
    ) : this(
        executiveSummary = if (samtykke.sammendrag) cv.sammendrag else "",
        employmentHistory = if (samtykke.arbeidserfaring) {
            EmploymentHistory(
                employerHistory = arbeidserfaringerWithEsco.map {
                    EmployerHistory(it)
                }
            )
        } else null,
        educationHistory = if (samtykke.utdanning) {
            EducationHistory(
                educationOrganizationAttendance = cv.utdannelse.map {
                    EducationOrganizationAttendance(
                        utdannelse = it
                    )
                }
            )
        } else null,
        licenses = if (samtykke.foererkort) {
            Licenses(
                license = cv.foererkort.klasse.map {
                    License(
                        foererkortKlasse = it
                    )
                }
            )
        } else null,
        certifications = Certifications(
            if (samtykke.offentligeGodkjenninger) {
                cv.godkjenninger.mapNotNull {
                    it.tittel ?: return@mapNotNull null
                    it.utsteder ?: return@mapNotNull null
                    Certification(it)
                }
            } else {
                listOf()
            }.plus(
                if (samtykke.andreGodkjenninger) {
                    cv.sertifikat.mapNotNull {
                        it.tittel ?: return@mapNotNull null
                        it.utsteder ?: return@mapNotNull null
                        Certification(it)
                    }
                } else {
                    listOf()
                }
            ).plus(
                if (samtykke.kurs) {
                    cv.kurs.mapNotNull {
                        it.tittel ?: return@mapNotNull null
                        it.utsteder ?: return@mapNotNull null
                        Certification(it)
                    }
                } else {
                    listOf()
                }
            ).plus(
                if (samtykke.offentligeGodkjenninger && samtykke.fagbrev) {
                    cv.fagdokumentasjon.mapNotNull {
                        it.tittel ?: return@mapNotNull null
                        Certification(it)
                    }
                } else if (samtykke.offentligeGodkjenninger) {
                    cv.fagdokumentasjon.mapNotNull {
                        it.tittel ?: return@mapNotNull null
                        if (it.type != FagdokumentasjonType.AUTORISASJON) return@mapNotNull null
                        Certification(it)
                    }
                } else if (samtykke.fagbrev) {
                    cv.fagdokumentasjon.mapNotNull {
                        it.tittel ?: return@mapNotNull null
                        if (it.type == FagdokumentasjonType.AUTORISASJON) return@mapNotNull null
                        Certification(it)
                    }
                } else {
                    listOf()
                }
            )
        ).nullIfNoCerts()
    )
}