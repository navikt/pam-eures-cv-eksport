package no.nav.cv.eures.konverterer

import no.nav.arbeid.cv.avro.Arbeidserfaring
import no.nav.arbeid.cv.avro.Cv
import no.nav.cv.eures.janzz.JanzzService
import no.nav.cv.eures.samtykke.Samtykke
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate


@SpringBootTest
@EnableMockOAuth2Server
internal class EmploymentHistoryConverterTest {

    @Autowired
    private lateinit var janzzService: JanzzService

    @Test
    fun `Arbeidserfaring med startdato null mappes til DateText unknown`() {
        val arbeidserfaringer = listOf(
            Arbeidserfaring(null, "Kul kokk med sluttdato", null, null, "NAV-Kantina", LocalDate.of(2022, 8, 26), null, null, null, true),
            Arbeidserfaring(null, "Kul kokk uten sluttdato", null, null, "NAV-Kantina", null, null, null, null, true)
        )

        val cv = Cv()
        cv.arbeidserfaring = arbeidserfaringer
        val samtykke = Samtykke(arbeidserfaring = true)
        val result = EmploymentHistoryConverter(cv, samtykke, janzzService).toXmlRepresentation()
        val serialisert = XmlSerializer.serialize(result!!)

        assertEquals(expectedXml, serialisert)
    }


    val expectedXml = """<?xml version='1.0' encoding='UTF-8'?>
<EmploymentHistory>
  <EmployerHistory>
    <OrganizationName>NAV-Kantina</OrganizationName>
    <EmploymentPeriod>
      <StartDate>
        <FormattedDateTime>2022-08-26</FormattedDateTime>
      </StartDate>
    </EmploymentPeriod>
    <PositionHistory>
      <PositionTitle>Kul kokk med sluttdato</PositionTitle>
      <EmploymentPeriod>
        <StartDate>
          <FormattedDateTime>2022-08-26</FormattedDateTime>
        </StartDate>
      </EmploymentPeriod>
    </PositionHistory>
  </EmployerHistory>
  <EmployerHistory>
    <OrganizationName>NAV-Kantina</OrganizationName>
    <EmploymentPeriod>
      <StartDate>
        <DateText>Unknown</DateText>
      </StartDate>
    </EmploymentPeriod>
    <PositionHistory>
      <PositionTitle>Kul kokk uten sluttdato</PositionTitle>
      <EmploymentPeriod>
        <StartDate>
          <DateText>Unknown</DateText>
        </StartDate>
      </EmploymentPeriod>
    </PositionHistory>
  </EmployerHistory>
</EmploymentHistory>

""".trimIndent()
}