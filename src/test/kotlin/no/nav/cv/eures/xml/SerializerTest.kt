package no.nav.cv.eures.xml

import no.nav.cv.eures.konverterer.XmlSerializer
import no.nav.cv.eures.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class SerializerTest {
    @Test
    fun ignoreInvalidChars() {
        val xml = XmlSerializer.serialize(candidate("Jajamens\u0000ann"))

        assertFalse(xml.contains('\u0000'))
        assertTrue(xml.contains("<ExecutiveSummary>Jajamens ann</ExecutiveSummary>"))
    }

    @Test
    fun `preserves EURES namespaces attributes text elements and unwrapped lists`() {
        val xml = XmlSerializer.serialize(candidate("Summary & details"))
        val document = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder().parse(InputSource(StringReader(xml)))
        val root = document.documentElement
        val hrNamespace = "http://www.hr-xml.org/3"
        val oaNamespace = "http://www.openapplications.org/oagis/9"
        val documentId = document.getElementsByTagNameNS(hrNamespace, "DocumentID").item(0)

        assertTrue(xml.startsWith("<?xml version='1.0' encoding='UTF-8'?>"))
        assertEquals("Candidate", root.localName)
        assertEquals(hrNamespace, root.namespaceURI)
        assertEquals(oaNamespace, root.getAttribute("xmlns:oa"))
        assertEquals("3", root.getAttribute("majorVersionID"))
        assertEquals("2", root.getAttribute("minorVersionID"))
        assertEquals("test-reference", documentId.textContent)
        assertEquals("NAV-002", documentId.attributes.getNamedItem("schemeID").nodeValue)
        assertEquals("Foo", document.getElementsByTagNameNS(oaNamespace, "GivenName").item(0).textContent)
        assertEquals(2, document.getElementsByTagNameNS(hrNamespace, "Communication").length)
        assertEquals("Summary & details", document.getElementsByTagNameNS(hrNamespace, "ExecutiveSummary").item(0).textContent)
        assertEquals(0, document.getElementsByTagNameNS(hrNamespace, "CandidatePositionPreferences").length)
    }

    private fun candidate(summary: String) = Candidate(
        documentId = DocumentId(uuid = "test-reference"),
        candidatePerson = CandidatePerson(
            personName = Name("Foo", "Bar"),
            communication = Communication.buildList(telephone = "12345678", email = "test@example.com"),
            birthDate = "2000-01-01",
            genderCode = GenderCode.NotSpecified,
            primaryLanguageCode = listOf("NO"),
            residencyCountryCode = "NO",
            nationalityCode = emptyList(),
        ),
        candidateSupplier = emptyList(),
        candidateProfile = CandidateProfile(executiveSummary = summary),
    )
}