package no.nav.cv.eures.xml

import org.junit.jupiter.api.Disabled
import no.nav.cv.eures.konverterer.CvConverterService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File

// TODO - Enable test again
@SpringBootTest
@ActiveProfiles("test")
class XmlSerializerTest {

    @Autowired
    lateinit var cvConverterService: CvConverterService

    @Test
    @Disabled
    fun `produce xml document`() {
        val aktorId = "10013106889"

        val xmlString = cvConverterService.convertToXml(aktorId)

        val filename = "cv_$aktorId.xml"
        // File(filename).writeText(xmlString.second)
    }
}
