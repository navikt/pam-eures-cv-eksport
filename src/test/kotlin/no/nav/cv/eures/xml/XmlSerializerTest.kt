package no.nav.cv.eures.xml

import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MicronautTest
import no.nav.cv.eures.konverterer.Konverterer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

// TODO - Enable test again
@Requires(env = ["SomeBogusEnvironmentThatDoesn'tExist"])
@MicronautTest
class XmlSerializerTest(
        private val konverterer: Konverterer
) {

    @Test
    @Disabled
    fun `produce xml document`() {
        val aktorId = "10013106889"

        val xmlString = konverterer.konverterTilXML(aktorId)

        val filename = "cv_$aktorId.xml"
        File(filename).writeText(xmlString.second)
    }
}
