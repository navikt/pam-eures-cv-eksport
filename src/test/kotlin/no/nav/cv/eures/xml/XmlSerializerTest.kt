package no.nav.cv.eures.xml

import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.RawCV
import no.nav.cv.eures.konverterer.CvConverterService
import no.nav.cv.eures.konverterer.CvConverterService2
import no.nav.cv.eures.samtykke.Samtykke
import no.nav.cv.eures.samtykke.SamtykkeControllerTest
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.time.ZonedDateTime

// TODO - Enable test again
//@SpringBootTest
//@ExtendWith(SpringExtension::class)
class XmlSerializerTest {

    @Autowired
    lateinit var cvConverterService2: CvConverterService2

    @Autowired
    lateinit var cvRepository: CvRepository

    @Autowired
    lateinit var samtykkeRepository: SamtykkeRepository


    @Test
    @Disabled
    fun `produce xml document`() {
        val fnr = "01019012345"

        val rawAvroBase64 = File("$fnr-RawAvroBase64.txt").readText()

        val newRawCv = RawCV.create(
                aktoerId = fnr,
                foedselsnummer = fnr,
                sistEndret = ZonedDateTime.now(),
                rawAvro = rawAvroBase64,
                underOppfoelging = false,
                meldingstype = RawCV.Companion.RecordType.CREATE
        )

        samtykkeRepository.oppdaterSamtykke(fnr,
                Samtykke(
                        personalia= true,
                        utdanning= true,
                        fagbrev= true,
                        arbeidserfaring= true,
                        annenErfaring= true,
                        foererkort= true,
                        lovregulerteYrker= true,
                        andreGodkjenninger= true,
                        kurs= true,
                        spraak= true,
                        sammendrag= true,
                        land = listOf()
                    ))

       // cvRepository.saveAndFlush(newRawCv)


        val xmlString = cvConverterService2.convertToXml(fnr)

        val filename = "cv_$fnr.xml"
        File(filename).writeText(xmlString!!.second)
    }
}
