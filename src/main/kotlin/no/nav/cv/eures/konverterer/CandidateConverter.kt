package no.nav.cv.eures.konverterer

import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.eures.model.Candidate
import no.nav.cv.eures.model.CandidateSupplier
import no.nav.cv.eures.model.DocumentId
import no.nav.cv.eures.samtykke.Samtykke

class CandidateConverter (
    private val dto : CvEndretInternDto,
    private val samtykke : Samtykke
) {

    fun toXmlRepresentation() = (
            Candidate(
                documentId = DocumentId(uuid = dto.cv?.uuid.toString()),
                candidateSupplier = CandidateSupplier().default(),
                candidatePerson = CandidatePersonConverter(dto, samtykke).toXmlRepresentation(),
                candidateProfile = CandidateProfileConverter(dto, samtykke).toXmlRepresentation()
            )
    )
}