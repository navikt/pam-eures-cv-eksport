package no.nav.cv.eures.konverterer

import no.nav.cv.dto.jobwishes.*
import no.nav.cv.eures.esco.EscoService
import no.nav.cv.eures.model.*
import no.nav.cv.eures.model.PositionOfferingTypes.*
import no.nav.cv.eures.model.PositionSchedule.*
import no.nav.cv.eures.samtykke.Samtykke

class CandidatePositionPreferencesConverter(
    private val samtykke: Samtykke,
    private val jobbønsker: CvEndretInternJobwishesDto?,
    private val escoService: EscoService = EscoService.instance()
) {

    fun toXmlRepresentation(): CandidatePositionPreferences = CandidatePositionPreferences(
        preferredLocation = samtykke.land.map { countryCode -> PreferredLocation(ReferenceLocation(countryCode = countryCode)) },
        jobCategory = if (samtykke.jobbonsker) jobbønsker?.occupations?.toXmlRepresentation() else null,
        positionOffering = if (samtykke.jobbonsker) jobbønsker?.occupationTypes?.toXmlRepresentation() else null,
        positionSchedule = if (samtykke.jobbonsker) jobbønsker?.workLoadTypes?.toXmlRepresentation() else null
    )

    @JvmName("toJobCategoryCode")
    private fun List<CvEndretInternOccupation>.toXmlRepresentation() = mapNotNull {
        escoService.hentEscoForKonseptId(it.conceptId.toString())?.tilJobCategoryCode()
    }.map { JobCategory(it) }

    @JvmName("toPositionOfferingTypeCode")
    private fun List<CvEndretInternOccupationType>.toXmlRepresentation() = mapNotNull {
        when (it.title) {
            Ansettelsesform.ENGASJEMENT,
            Ansettelsesform.VIKARIAT,
            Ansettelsesform.TRAINEE,
            Ansettelsesform.ANNET,
            Ansettelsesform.PROSJEKT -> PositionOfferingTypeCode(name = Temporary.name, code = Temporary.name)
            Ansettelsesform.FERIEJOBB,
            Ansettelsesform.SESONG -> PositionOfferingTypeCode(name = Seasonal.name, code = Seasonal.name)
            Ansettelsesform.SELVSTENDIG_NAERINGSDRIVENDE -> PositionOfferingTypeCode(name = SelfEmployed.name, code = SelfEmployed.name)
            Ansettelsesform.LAERLING -> PositionOfferingTypeCode(name = TemporaryToHire.name, code = TemporaryToHire.name)
            Ansettelsesform.FAST -> PositionOfferingTypeCode(name = DirectHire.name, code = DirectHire.name)
            null -> null
        }
    }.distinctBy { it.code }

    @JvmName("toPositionScheduleCode")
    private fun List<CvEndretInternWorkLoadType>.toXmlRepresentation() = mapNotNull {
        when (it.title) {
            Omfang.HELTID -> PositionScheduleCode(name = FullTime.name, code = FullTime.name)
            Omfang.DELTID -> PositionScheduleCode(name = PartTime.name, code = PartTime.name)
            null -> null
        }
    }.distinctBy { it.code }
}
