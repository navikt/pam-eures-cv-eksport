package no.nav.cv.eures.konverterer

import no.nav.cv.eures.samtykke.Samtykke
import org.junit.Assert
import org.junit.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CandidatePositionPreferencesConverterTest {

    @Test
    fun `test konvertering til xml`() {
        var samtykke = Samtykke(personalia = true, land= listOf<String>("DE", "NO"))
        var converter = CandidatePositionPreferencesConverter(samtykke)
        Assert.assertEquals(2, converter.toXmlRepresentation()?.preferredLocations?.size)
        Assert.assertEquals("DE", converter.toXmlRepresentation()?.preferredLocations?.get(0)?.referenceLocation?.countryCode)
        Assert.assertEquals("NO", converter.toXmlRepresentation()?.preferredLocations?.get(1)?.referenceLocation?.countryCode)
    }
}
