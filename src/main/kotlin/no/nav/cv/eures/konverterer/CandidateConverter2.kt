package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Jobbprofil
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.eures.model.Candidate
import no.nav.cv.eures.model.CandidateSupplier
import no.nav.cv.eures.model.DocumentId
import no.nav.cv.eures.samtykke.Samtykke

class CandidateConverter2 (
    private val dto : CvEndretInternDto,
    private val samtykke : Samtykke
) {

    fun toXmlRepresentation() = (
            Candidate(
                documentId = DocumentId(uuid = dto.cv?.uuid.toString()),
                candidateSupplier = CandidateSupplier().default(),
                candidatePerson = CandidatePersonConverter2(dto, samtykke).toXmlRepresentation(),
                candidateProfile = CandidateProfileConverter2(dto, samtykke).toXmlRepresentation()
            )
    )
}