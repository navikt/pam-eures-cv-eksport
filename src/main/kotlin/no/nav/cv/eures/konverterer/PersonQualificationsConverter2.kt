package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.*
import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.cv.CvEndretInternLanguage
import no.nav.cv.dto.cv.CvEndretInternSkillDraft
import no.nav.cv.dto.jobwishes.CvEndretInternSkill
import no.nav.cv.eures.janzz.JanzzService
import no.nav.cv.eures.konverterer.language.LanguageConverter
import no.nav.cv.eures.model.PersonCompetency
import no.nav.cv.eures.model.PersonQualifications
import no.nav.cv.eures.samtykke.Samtykke
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PersonQualificationsConverter2(
    private val dto: CvEndretInternDto,
    private val samtykke: Samtykke,
    private val janzzService: JanzzService = JanzzService.instance()
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PersonQualificationsConverter2::class.java)
    }

    fun toXmlRepresentation(): PersonQualifications? {
        val qualifications = mutableListOf<PersonCompetency>()

        if (samtykke.spraak)
            qualifications.addAll(dto.cv?.languages?.toLanguages().orEmpty())

        if (samtykke.kompetanser) {
            qualifications.addAll(dto.cv?.skillDrafts?.mapNotNull { it.title }?.toEsco().orEmpty())
            qualifications.addAll(dto.jobWishes?.skills?.mapNotNull { it.title }?.toEsco().orEmpty())
        }
        return if (qualifications.isNotEmpty()) PersonQualifications(qualifications) else null
    }

    private fun List<CvEndretInternLanguage>.toLanguages(): List<PersonCompetency> = mapNotNull { spraak ->
        spraak.iso3Code
            ?.let { i3k -> LanguageConverter.fromIso3ToIso1(i3k) }
            ?.let { PersonCompetency(competencyID = it, taxonomyID = "language") }
    }


    @JvmName("toEscoKompetanser")
    private fun List<String>.toEsco(): List<PersonCompetency> = asSequence()
        .map { janzzService.getEscoForTerm(it, JanzzService.EscoLookupType.SKILL) }
        .flatten()
        .map { PersonCompetency(competencyID = it.esco, taxonomyID = "other") }.toList()

}
