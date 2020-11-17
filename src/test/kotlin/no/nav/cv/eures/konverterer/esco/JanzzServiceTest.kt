package no.nav.cv.eures.konverterer.esco

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
internal class JanzzServiceTest {
    @Autowired
    private lateinit var janzzService: JanzzService

    // Hard to test towards Janzz since their data is evolving, but this gives us the ability to check manually
    @Test
    @Disabled
    fun `test search`() {
        println(janzzService.getEscoForCompetence("Programmering"))
        println(janzzService.getEscoForCompetence("Programmering"))
    }
}