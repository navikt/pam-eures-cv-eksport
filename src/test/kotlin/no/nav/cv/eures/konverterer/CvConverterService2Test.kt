package no.nav.cv.eures.konverterer

import no.nav.cv.eures.cv.CvXml
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.ZonedDateTime

@SpringBootTest
@EnableMockOAuth2Server
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
internal class CvConverterService2Test {

    @Autowired
    lateinit var cvConverterService: CvConverterService2

    @Test
    fun `Not changing checksum when xml is not changed`() {
        val cvBefore = createCv("Ref321", "111222333")

        val checksumBefore = cvBefore.checksum
        val cvAfter = cvConverterService.updateIfChanged(cvBefore, xmlString1)

        assertEquals(checksumBefore, cvAfter.checksum)
        assertEquals(createdAt, cvAfter.sistEndret)
    }

    @Test
    fun `Updated checksum when xml is changed`() {
        val cvBefore = createCv("Ref123", "333222111")

        val checksumBefore = cvBefore.checksum
        val checksumAfter = cvConverterService.md5(xmlString2)

        val cvAfter = cvConverterService.updateIfChanged(cvBefore, xmlString2)

        assertNotEquals(checksumBefore, cvAfter.checksum)
        assertEquals(checksumAfter, cvAfter.checksum)
        assertTrue(createdAt.isBefore(cvAfter.sistEndret))
    }


    val createdAt = ZonedDateTime.now().minusMinutes(5)

    val xmlString2 = """
        <?xml version='1.0' encoding='UTF-8'?>
        <Candidate xmlns="http://www.hr-xml.org/3" xmlns:oa="http://www.openapplications.org/oagis/9" majorVersionID="3" minorVersionID="2">
          <DocumentID schemeID="NAV-002" schemeAgencyID="NAV" schemeAgencyName="NAV public employment services" schemeVersionID="1.3">4d8cea6e-3119-4e02-b98f-0d295ea1db2d</DocumentID>
          <CandidateSupplier>
            <PartyID>NAV.NO</PartyID>
            <PartyName>Nav</PartyName>
            <PersonContact>
              <PersonName>
                <oa:GivenName>Arbeidsplassen.no</oa:GivenName>
                <FamilyName>Arbeidsplassen.no</FamilyName>
              </PersonName>
              <Communication>
                <ChannelCode>Telephone</ChannelCode>
                <oa:DialNumber>nav.team.arbeidsplassen@nav.no</oa:DialNumber>
              </Communication>
            </PersonContact>
            <PrecedenceCode>1</PrecedenceCode>
          </CandidateSupplier>
          <CandidatePerson>
            <PersonName>
              <oa:GivenName>Klartenkt</oa:GivenName>
              <FamilyName>Fasade</FamilyName>
            </PersonName>
            <BirthDate>1998-10-21</BirthDate>
            <GenderCode>NotSpecified</GenderCode>
          </CandidatePerson>
          <CandidateProfile languageCode="no">
            <ExecutiveSummary></ExecutiveSummary>
            <EmploymentHistory>
              <EmployerHistory>
                <OrganizationName>Oslo Universitetssykehus</OrganizationName>
                <EmploymentPeriod>
                  <StartDate>
                    <FormattedDateTime>2017-04-01</FormattedDateTime>
                  </StartDate>
                </EmploymentPeriod>
                <PositionHistory>
                  <PositionTitle>Anestesisykepleier</PositionTitle>
                  <EmploymentPeriod>
                    <StartDate>
                      <FormattedDateTime>2017-04-01</FormattedDateTime>
                    </StartDate>
                  </EmploymentPeriod>
                  <JobCategoryCode listName="ESCO_Occupations" listURI="https://ec.europa.eu/esco/portal" listSchemeURI="https://ec.europa.eu/esco/portal" listVersionID="ESCOv1" name="anestesisykepleier">http://data.europa.eu/esco/occupation/18e14e61-495b-44cc-a7c6-df4c625934ba</JobCategoryCode>
                </PositionHistory>
              </EmployerHistory>
            </EmploymentHistory>
            <Certifications>
              <Certification>
                <CertificationName>Sveisemetode 111 - Dekkede elektroder</CertificationName>
                <IssuingAuthortity>
                  <Name></Name>
                </IssuingAuthortity>
                <FirstIssuedDate>
                  <FormattedDateTime>2018-04-01</FormattedDateTime>
                </FirstIssuedDate>
                <FreeFormEffectivePeriod/>
              </Certification>
              <Certification>
                <CertificationName>Fagbrev helsefagarbeider</CertificationName>
                <IssuingAuthortity>
                  <Name>Yrkesopplæringsnemnd</Name>
                </IssuingAuthortity>
              </Certification>
            </Certifications>
            <PersonQualifications>
            </PersonQualifications>
          </CandidateProfile>
        </Candidate>
    """.trimIndent()

    private val xmlString1 = """
        <?xml version='1.0' encoding='UTF-8'?>
        <Candidate xmlns="http://www.hr-xml.org/3" xmlns:oa="http://www.openapplications.org/oagis/9" majorVersionID="3" minorVersionID="2">
          <DocumentID schemeID="NAV-002" schemeAgencyID="NAV" schemeAgencyName="NAV public employment services" schemeVersionID="1.3">4d8cea6e-3119-4e02-b98f-0d295ea1db2d</DocumentID>
          <CandidateSupplier>
            <PartyID>NAV.NO</PartyID>
            <PartyName>Nav</PartyName>
            <PersonContact>
              <PersonName>
                <oa:GivenName>Arbeidsplassen.no</oa:GivenName>
                <FamilyName>Arbeidsplassen.no</FamilyName>
              </PersonName>
              <Communication>
                <ChannelCode>Telephone</ChannelCode>
                <oa:DialNumber>nav.team.arbeidsplassen@nav.no</oa:DialNumber>
              </Communication>
            </PersonContact>
            <PrecedenceCode>1</PrecedenceCode>
          </CandidateSupplier>
          <CandidatePerson>
            <PersonName>
              <oa:GivenName>Fin</oa:GivenName>
              <FamilyName>Hest</FamilyName>
            </PersonName>
            <BirthDate>1998-10-21</BirthDate>
            <GenderCode>NotSpecified</GenderCode>
          </CandidatePerson>
          <CandidateProfile languageCode="no">
            <ExecutiveSummary></ExecutiveSummary>
            <EmploymentHistory>
            </EmploymentHistory>
            <Certifications>
              <Certification>
                <CertificationName>Sveisemetode 111 - Dekkede elektroder</CertificationName>
                <IssuingAuthortity>
                  <Name></Name>
                </IssuingAuthortity>
                <FirstIssuedDate>
                  <FormattedDateTime>2018-04-01</FormattedDateTime>
                </FirstIssuedDate>
                <FreeFormEffectivePeriod/>
              </Certification>
            </Certifications>
            <PersonQualifications>
            </PersonQualifications>
          </CandidateProfile>
        </Candidate>
    """.trimIndent()

    private fun createCv(ref: String, fnr: String) = CvXml.create(
        reference = ref,
        foedselsnummer = fnr,
        opprettet = createdAt,
        sistEndret = createdAt,
        slettet = null,
        xml = xmlString1,
        checksum = cvConverterService.md5(xmlString1)
    )
}