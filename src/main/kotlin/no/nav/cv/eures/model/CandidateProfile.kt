package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Jobbprofil
import no.nav.cv.eures.janzz.dto.CachedEscoMapping
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
    constructor(cv: Cv, jobbProfil: Jobbprofil, samtykke: Samtykke, occupationEscoMapping: CachedEscoMapping): this(
        executiveSummary = if(samtykke.sammendrag) cv.sammendrag else "",
        employmentHistory = if(samtykke.arbeidserfaring) {
            EmploymentHistory(
                employerHistory = cv.arbeidserfaring.map {
                    EmployerHistory(
                        arbeidserfaring = it,
                        occupationEscoMapping = occupationEscoMapping
                    )
                }
            )
        } else null,
        educationHistory = if(samtykke.utdanning) {
            EducationHistory(
                educationOrganizationAttendance = cv.utdannelse.map {
                    EducationOrganizationAttendance(
                        utdannelse = it
                    )
                }
            )
        } else null,
        licenses = if(samtykke.foererkort) {
            Licenses(
                license = cv.foererkort.klasse.map {
                    License(
                        foererkortKlasse = it
                    )
                }
            )
        } else null,

    )
}