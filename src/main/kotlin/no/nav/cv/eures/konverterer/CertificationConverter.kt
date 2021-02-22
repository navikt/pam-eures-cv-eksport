package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Godkjenning
import no.nav.cv.eures.model.Certification
import no.nav.cv.eures.model.Certifications
import no.nav.cv.eures.model.FreeFormEffectivePeriod
import no.nav.cv.eures.model.IssuingAuthority
import no.nav.cv.eures.samtykke.Samtykke

class CertificationConverter(
        private val cv: Cv,
        private val samtykke: Samtykke
) {
    private val ikkeSamtykket = null

    fun toXmlRepresentation() = when (samtykke.andreGodkjenninger) {
        true -> cv.godkjenninger.toGodkjenninger()
        false -> ikkeSamtykket
    }

    private fun List<Godkjenning>.toGodkjenninger()
    = Certifications(map {
        Certification(
                certificationTypeCode = null, // TODO: Find out what this should be
                certificationName = it.tittel,
                issuingAuthortity = IssuingAuthority(it.utsteder),
                firstIssuedDate = it.gjennomfoert.toFormattedDateTime(),
                freeFormEffectivePeriod = FreeFormEffectivePeriod(
                        startDate = null,
                        endDate = it.utloeper.toFormattedDateTime()
                )
        )
    }
    )
}