package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.cv.eures.model.Candidate
import no.nav.cv.eures.model.CandidateSupplier
import no.nav.cv.eures.model.DocumentId
import no.nav.cv.eures.samtykke.Samtykke

class CandidateConverter (
        private val cv: Cv,
        private val samtykke: Samtykke
) {
    fun toXmlRepresentation()
            = Candidate(
                documentId = DocumentId(uuid = cv.cvId),
                candidateSupplier = CandidateSupplier().default(),
                candidatePerson = CandidatePersonConverter(cv, samtykke).toXmlRepresentation(),
                candidateProfile = CandidateProfileConverter(cv, samtykke).toXmlRepresentation()


    )
}