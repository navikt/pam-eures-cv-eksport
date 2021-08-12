package no.nav.cv.eures.konverterer.esco

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

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
        println(janzzService.getEscoForCompetence("Programmering"))
        println(janzzService.getEscoForCompetence("Programmering"))

        println(janzzService.getEscoForCompetence("NO HIT"))
        println(janzzService.getEscoForCompetence("NO HIT"))

        println(janzzService.getEscoForConceptTitle("Førstestyrmann"))
        println(janzzService.getEscoForConceptTitle("Førstestyrmann"))
    }
}