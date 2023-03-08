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
    private val ikkeSamtykket = null

    companion object {
        val log: Logger = LoggerFactory.getLogger(LicensesConverter::class.java)
        val licenseTypeCodes =
            listOf("AM", "A1", "A2", "A", "B1", "B", "BE", "C1", "C1E", "C", "CE", "D1", "D1E", "DE", "D");
    }

    fun toXmlRepresentation(): Licenses? {
        return dto.cv?.driversLicenses?.toLicenses()
    }

    fun List<CvEndretInternDriversLicence>.toLicenses(): Licenses {
        return Licenses(filter { cvEndretInternDriversLicence1 ->
            licenseTypeCodes.contains(
                cvEndretInternDriversLicence1.klasse
            )
        }
            .map { cvEndretInternDriversLicence ->
                License(
                    licenseTypeCode = cvEndretInternDriversLicence.klasse ?: "",
                    licenseName = cvEndretInternDriversLicence.description ?: "",
                    freeFormPeriod = FreeFormPeriod(
                        startDate = cvEndretInternDriversLicence.acquiredDate?.toFormattedDateTime(),
                        endDate = cvEndretInternDriversLicence.expiryDate?.toFormattedDateTime()
                    )
                )
            }
        )
    }
}

