package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

// 4.5
data class CandidateSupplier(
        @JacksonXmlProperty(localName = "PartyID")
        val partyId: String = "NAV.NO",
        val partyName: String = "Nav",

        @JacksonXmlElementWrapper(useWrapping = false)
        val personContact: List<PersonContact>  = listOf(
                PersonContact(
                        personName = Name(
                                givenName = "Arbeidsplassen.no",
                                familyName = "Arbeidsplassen.no"),
                        communication = Communication.buildList(telephone = "nav.team.arbeidsplassen@nav.no"))),

        val precedenceCode: Int = 1
) {
        fun default() = listOf(this)
}

