package no.nav.cv.eures.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import no.nav.arbeid.cv.avro.Arbeidserfaring
import no.nav.cv.eures.janzz.JanzzService
import no.nav.cv.eures.janzz.dto.CachedEscoMapping
import no.nav.cv.eures.konverterer.toFormattedDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

data class EmploymentHistory(
        @JacksonXmlElementWrapper(useWrapping = false)
        val employerHistory: List<EmployerHistory>,
) {
    data class EmployerHistory(
        val organizationName: String,
        val employmentPeriod: AttendancePeriod,

        @JacksonXmlElementWrapper(useWrapping = false)
        val positionHistory: List<PositionHistory>
    ) {
        constructor(arbeidserfaring: Arbeidserfaring): this(
            organizationName = arbeidserfaring.arbeidsgiver ?: "",
            employmentPeriod = AttendancePeriod(
                arbeidserfaring.fraTidspunkt.toFormattedDateTime(),
                arbeidserfaring.tilTidspunkt?.toFormattedDateTime()
            ),
            positionHistory = listOf(
                PositionHistory(
                    arbeidserfaring = arbeidserfaring,
                )
            )
        )
    }
    class PositionHistory(
        val positionTitle: String,
        val employmentPeriod: AttendancePeriod,
        val jobCategoryCode: JobCategoryCode?
    ) {
        @Autowired
        private lateinit var janzzService: JanzzService

        constructor(arbeidserfaring: Arbeidserfaring): this(
            //motsatt rekkefolge?
            positionTitle = arbeidserfaring.stillingstittel ?: arbeidserfaring.stillingstittelFritekst,
            employmentPeriod = AttendancePeriod(
                arbeidserfaring.fraTidspunkt.toFormattedDateTime(),
                arbeidserfaring.tilTidspunkt?.toFormattedDateTime()
            ),
            jobCategoryCode = JobCategoryCode(janzzService)
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
}