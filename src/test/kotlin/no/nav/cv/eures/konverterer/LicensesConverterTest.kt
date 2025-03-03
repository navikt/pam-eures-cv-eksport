package no.nav.cv.eures.konverterer

import no.nav.cv.dto.CvEndretInternDto
import no.nav.cv.dto.CvMeldingstype
import no.nav.cv.dto.cv.CvEndretInternCvDto
import no.nav.cv.dto.cv.CvEndretInternDriversLicence
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class LicensesConverterTest {
    @Test
    fun klasseBlirTilRiktigTypecode() {
        val klasseListe = listOf("B - Personbil", "B 78 - Personbil med automatgir", "C1E - Lett lastebil med tilhenger", "Tull")
        val førerkortListe = klasseListe.map { CvEndretInternDriversLicence(it, null, null, null) }

        val dto = cvDtoMedFørerkort(førerkortListe)

        val converter = LicensesConverter(dto)
        val konverterteFørerkort = converter.toXmlRepresentation()!!.license

        assertEquals(2, konverterteFørerkort.size)
        assertTrue(konverterteFørerkort.any { it.licenseTypeCode == "B" })
        assertTrue(konverterteFørerkort.any { it.licenseTypeCode == "C1E" })
        assertFalse { konverterteFørerkort.any { it.licenseTypeCode == "B 78" } }
        assertFalse { konverterteFørerkort.any { it.licenseTypeCode == "Tull" } }
    }

    private fun cvDtoMedFørerkort(førerkort: List<CvEndretInternDriversLicence>) = CvEndretInternDto(
        "1".repeat(13),
        null,
        null,
        CvMeldingstype.ENDRE,
        lagCvEndretInternCvDto(førerkort),
        null,
        null,
        null,
        null
    )

    private fun lagCvEndretInternCvDto(førerkort: List<CvEndretInternDriversLicence>) = CvEndretInternCvDto(
        UUID.randomUUID(),
        true,
        null,
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        førerkort,
        emptyList(),
        false,
        false,
        null,
        null
    )
}
