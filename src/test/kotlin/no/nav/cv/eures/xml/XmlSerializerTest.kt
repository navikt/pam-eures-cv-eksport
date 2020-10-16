package no.nav.cv.eures.xml

import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Disabled
import no.nav.cv.eures.konverterer.CvConverterService
import org.junit.jupiter.api.Test
import java.io.File

// TODO - Enable test again
@MicronautTest
class XmlSerializerTest(
        private val cvConverterService: CvConverterService
) {

    @Test
    @Disabled
    fun `produce xml document`() {
        val aktorId = "10013106889"

        val xmlString = cvConverterService.convertToXml(aktorId)

        val filename = "cv_$aktorId.xml"
        // File(filename).writeText(xmlString.second)
    }
}
