package no.nav.cv.eures.konverterer

import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.cv.CvEndretInternDriversLicence
import no.nav.cv.eures.model.FreeFormPeriod
import no.nav.cv.eures.model.License
import no.nav.cv.eures.model.Licenses
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LicensesConverter(
    private val dto: CvEndretInternDto
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(LicensesConverter::class.java)
        val euresAcceptedLicenseTypeCodes =
            listOf("AM", "A1", "A2", "A", "B1", "B", "BE", "C1", "C1E", "C", "CE", "D1", "D1E", "DE", "D");
    }

    fun toXmlRepresentation(): Licenses? {
        return dto.cv?.driversLicenses?.toLicenses()
    }

    fun List<CvEndretInternDriversLicence>.toLicenses() = Licenses(
        mapNotNull { cvInternLicence ->
            val licenseTypeCode = cvInternLicence.klasse?.split(" - ")?.first() ?: ""

            if (!euresAcceptedLicenseTypeCodes.contains(licenseTypeCode)) return@mapNotNull null

            License(
                licenseTypeCode = licenseTypeCode,
                licenseName = cvInternLicence.description ?: "",
                freeFormPeriod = FreeFormPeriod(
                    startDate = cvInternLicence.acquiredDate?.toFormattedDateTime(),
                    endDate = cvInternLicence.expiryDate?.toFormattedDateTime()
                )
            )
        })
}
