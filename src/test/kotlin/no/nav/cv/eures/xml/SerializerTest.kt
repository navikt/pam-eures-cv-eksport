package no.nav.cv.eures.xml

import no.nav.cv.eures.konverterer.XmlSerializer
import no.nav.cv.eures.model.*
import org.junit.jupiter.api.Test
import java.util.*

class SerializerTest {
    @Test
    fun ignoreInvalidChars() {
        val c = Candidate(documentId = DocumentId(uuid=UUID.randomUUID().toString()),
            candidatePerson = CandidatePerson(personName = Name("Foo", "Bar"),
                    communication = emptyList(),
                    birthDate = "2000-01-01",
                    genderCode = GenderCode.NotSpecified,
                    primaryLanguageCode = listOf("NO"),
                    residencyCountryCode = "NO",
                    nationalityCode = emptyList()),
            candidateSupplier = emptyList(),
            candidateProfile = CandidateProfile(executiveSummary = "Jajamens\u0000ann"))
        XmlSerializer.serialize(c)
    }
}