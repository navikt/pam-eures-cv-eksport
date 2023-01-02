package no.nav.cv.eures.konverterer

import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.cv.CvEndretInternDriversLicence
import no.nav.cv.eures.model.FreeFormPeriod
import no.nav.cv.eures.model.License
import no.nav.cv.eures.model.Licenses
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LicensesConverter (
        private val dto: CvEndretInternDto
) {
    private val ikkeSamtykket = null

    companion object {
        val log: Logger = LoggerFactory.getLogger(LicensesConverter::class.java)
    }

    fun toXmlRepresentation() : Licenses? {
        return dto.cv?.driversLicenses?.toLicenses()
    }

    fun List<CvEndretInternDriversLicence>.toLicenses() : Licenses{
        return Licenses(map { cvEndretInternDriversLicence ->  License(
            licenseTypeCode = cvEndretInternDriversLicence.klasse ?: "",
            licenseName = cvEndretInternDriversLicence.description ?: "",
            freeFormPeriod = FreeFormPeriod(
                startDate = cvEndretInternDriversLicence.acquiredDate?.toFormattedDateTime(),
                endDate = cvEndretInternDriversLicence.expiryDate?.toFormattedDateTime()
            )
        )})
    }

}