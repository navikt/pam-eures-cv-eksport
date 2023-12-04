package no.nav.cv.eures.konverterer.esco

import no.nav.cv.eures.janzz.JanzzService
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

//@SpringBootTest
//@ActiveProfiles("test")
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
internal class JanzzServiceTest {
    @Autowired
    private lateinit var janzzService: JanzzService

    // Hard to test towards Janzz since their data is evolving, but this gives us the ability to check manually
    @Test
    @Disabled
    fun `test search`() {
        println(janzzService.getEscoForTerm("Programmering", JanzzService.EscoLookupType.SKILL))
        println(janzzService.getEscoForTerm("Programmering", JanzzService.EscoLookupType.SKILL))

        println(janzzService.getEscoForTerm("Førstestyrmann", JanzzService.EscoLookupType.OCCUPATION))
        println(janzzService.getEscoForTerm("Førstestyrmann", JanzzService.EscoLookupType.OCCUPATION))
    }
}