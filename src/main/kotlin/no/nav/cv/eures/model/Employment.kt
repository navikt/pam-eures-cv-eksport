package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import no.nav.cv.eures.janzz.dto.CachedEscoMapping
import no.nav.cv.eures.konverterer.toFormattedDateTime
import no.nav.cv.eures.model.dto.ArbeidserfaringWithEscoDto

data class EmploymentHistory(
    @JacksonXmlElementWrapper(useWrapping = false)
    val employerHistory: List<EmployerHistory>
)

data class EmployerHistory(
    val organizationName: String,
    val employmentPeriod: AttendancePeriod,

    @JacksonXmlElementWrapper(useWrapping = false)
    val positionHistory: List<PositionHistory>
) {
    constructor(arbeidserfaringWithEsco: ArbeidserfaringWithEscoDto): this(
        organizationName = arbeidserfaringWithEsco.arbeidserfaring.arbeidsgiver ?: "",
        employmentPeriod = AttendancePeriod(
            arbeidserfaringWithEsco.arbeidserfaring.fraTidspunkt.toFormattedDateTime(),
            arbeidserfaringWithEsco.arbeidserfaring.tilTidspunkt?.toFormattedDateTime()
        ),
        positionHistory = listOf(
            PositionHistory(
                arbeidserfaringWithEsco = arbeidserfaringWithEsco,
            )
        )
    )
}
data class PositionHistory(
    val positionTitle: String,
    val employmentPeriod: AttendancePeriod,
    val jobCategoryCode: JobCategoryCode?
) {
    constructor(arbeidserfaringWithEsco: ArbeidserfaringWithEscoDto): this(
        //motsatt rekkefolge?
        positionTitle = arbeidserfaringWithEsco.arbeidserfaring.let {
            it.stillingstittel ?: it.stillingstittelFritekst
        },
        employmentPeriod = AttendancePeriod(
            arbeidserfaringWithEsco.arbeidserfaring.fraTidspunkt.toFormattedDateTime(),
            arbeidserfaringWithEsco.arbeidserfaring.tilTidspunkt?.toFormattedDateTime()
        ),
        jobCategoryCode = JobCategoryCode(arbeidserfaringWithEsco.occupationEscoMapping)
    )
}

data class JobCategoryCode(
    @JacksonXmlProperty(isAttribute = true, localName = "listName")
    val listName: String = "ESCO_Occupations",

    @JacksonXmlProperty(isAttribute = true, localName = "listURI")
    val listURI: String = "https://ec.europa.eu/esco/portal",

    @JacksonXmlProperty(isAttribute = true, localName = "listSchemeURI")
    val listSchemeURI: String ="https://ec.europa.eu/esco/portal",

    @JacksonXmlProperty(isAttribute = true, localName = "listVersionID")
    val listVersionID: String = "ESCOv1",

    @JacksonXmlProperty(isAttribute = true, localName = "name")
    val name: String,

    @JacksonXmlText
    val code: String
) {
    constructor(occupationEscoMapping: CachedEscoMapping): this(
        name = occupationEscoMapping.term,
        code = occupationEscoMapping.esco
    )
}