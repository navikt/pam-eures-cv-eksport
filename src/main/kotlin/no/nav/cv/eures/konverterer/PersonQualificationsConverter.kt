package no.nav.cv.eures.konverterer

import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.cv.CvEndretInternLanguage
import no.nav.cv.eures.esco.EscoService
import no.nav.cv.eures.esco.dto.EscoKodeType
import no.nav.cv.eures.konverterer.language.LanguageConverter
import no.nav.cv.eures.model.PersonCompetency
import no.nav.cv.eures.model.PersonQualifications
import no.nav.cv.eures.samtykke.Samtykke
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PersonQualificationsConverter(
    private val dto: CvEndretInternDto,
    private val samtykke: Samtykke,
    private val escoService: EscoService = EscoService.instance()
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PersonQualificationsConverter::class.java)
    }

    fun toXmlRepresentation(): PersonQualifications? {
        val qualifications = mutableListOf<PersonCompetency>()

        if (samtykke.spraak)
            qualifications.addAll(dto.cv?.languages?.toLanguages().orEmpty())

        if (samtykke.kompetanser) {
            qualifications.addAll(dto.jobWishes?.skills?.mapNotNull { it.conceptId }?.toEsco().orEmpty())
        }
        return if (qualifications.isNotEmpty()) PersonQualifications(qualifications) else null
    }

    private fun List<CvEndretInternLanguage>.toLanguages(): List<PersonCompetency> = mapNotNull { spraak ->
        spraak.iso3Code
            ?.let { i3k -> LanguageConverter.fromIso3ToIso1(i3k) }
            ?.let { PersonCompetency(competencyID = it, taxonomyID = "language") }
    }


    @JvmName("toEscoKompetanser")
    private fun List<String>.toEsco(): List<PersonCompetency> = flatMap { escoService.hentEscoForKonseptId(it) }
        .filter { it.type == EscoKodeType.ESCO }
        .map { PersonCompetency(competencyID = it.kode, taxonomyID = "other") }
}
