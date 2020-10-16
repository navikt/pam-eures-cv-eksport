package no.nav.cv.eures.xml

import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MicronautTest
import no.nav.cv.eures.konverterer.CvConverterService
import org.junit.jupiter.api.Test
import java.io.File

// TODO - Enable test again
@Requires(env = ["SomeBogusEnvironmentThatDoesn'tExist"])
@MicronautTest
class XmlSerializerTest(
        private val cvConverterService: CvConverterService
) {

    @Test
    fun `produce xml document`() {
        val aktorId = "10013106889"

        val xmlString = cvConverterService.convertToXml(aktorId)

        val filename = "cv_$aktorId.xml"
        // File(filename).writeText(xmlString.second)
    }
}
