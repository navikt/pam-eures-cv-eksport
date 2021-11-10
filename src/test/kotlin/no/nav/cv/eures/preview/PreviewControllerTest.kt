package no.nav.cv.eures.preview

import no.nav.cv.eures.cv.CvRepository
import no.nav.cv.eures.cv.CvTestData
import no.nav.cv.eures.cv.RawCV
import no.nav.cv.eures.konverterer.CvConverterService
import no.nav.cv.eures.samtykke.Samtykke
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles("test")
class PreviewControllerTest {

    @Autowired
    lateinit var cvRepository: CvRepository

    @Autowired
    lateinit var previewController: PreviewController

    @Autowired
    lateinit var cvConverterService: CvConverterService

    @Autowired
    lateinit var samtykkeRepository: SamtykkeRepository

    private val testData = CvTestData()

    @Test
    fun `test at controlleren returnerer en json`() {
        val samtykke = Samtykke(ZonedDateTime.now().minusDays(1), personalia = true, utdanning = false, kompetanser = true)
        samtykkeRepository.oppdaterSamtykke(testData.foedselsnummer1, samtykke)
        val cv1 = RawCV.create(testData.aktoerId1, testData.foedselsnummer1,
            testData.now, testData.rawAvro1Base64, false, RawCV.Companion.RecordType.CREATE)
        cvRepository.saveAndFlush(cv1)
        cvConverterService.createOrUpdate(testData.foedselsnummer1)

        val hei = previewController.getPreview(testData.foedselsnummer1)
        println(hei)
        println("AAAAAAAAAAA")
    }
}