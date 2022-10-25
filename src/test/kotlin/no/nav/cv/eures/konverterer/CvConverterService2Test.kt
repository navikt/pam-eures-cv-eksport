package no.nav.cv.eures.konverterer

import no.nav.cv.eures.cv.CvXml
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class CvConverterService2Test {

    @Autowired
    lateinit var cvConverterService: CvConverterService2

    @Test
    fun `Not changing checksum when xml is not changed`() {
        val cvBefore = createCv("Ref 1", "Fnr 1")

        val checksumBefore = cvBefore.checksum
        val cvAfter = cvConverterService.updateIfChanged(cvBefore, xmlString1)

        Assertions.assertEquals(checksumBefore, cvAfter.checksum)
        Assertions.assertEquals(createdAt, cvAfter.sistEndret)
    }

    @Test
    fun `Updated checksum when xml is changed`() {
        val cvBefore = createCv("Ref 2", "Fnr 2")

        val checksumBefore = cvBefore.checksum
        val checksumAfter = cvConverterService.md5(xmlString2)

        val cvAfter = cvConverterService.updateIfChanged(cvBefore, xmlString2)

        Assertions.assertNotEquals(checksumBefore, cvAfter.checksum)
        Assertions.assertEquals(checksumAfter, cvAfter.checksum)
        Assertions.assertTrue(createdAt.isBefore(cvAfter.sistEndret))
    }

    private fun createCv(ref: String, fnr: String) = CvXml.create(
        reference = ref,
        foedselsnummer = fnr,
        opprettet = createdAt,
        sistEndret = createdAt,
        slettet = null,
        xml = xmlString1,
        checksum = cvConverterService.md5(xmlString1)
    )

    val createdAt = ZonedDateTime.now().minusMinutes(5)

    val xmlString1 = "XML String 1"
    val xmlString2 = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<Candidate xmlns=\"http://www.hr-xml.org/3\" xmlns:oa=\"http://www.openapplications.org/oagis/9\" majorVersionID=\"3\" minorVersionID=\"2\">\n" +
            "    <DocumentID schemeID=\"NAV-002\" schemeAgencyID=\"NAV\" schemeAgencyName=\"NAV public employment services\" schemeVersionID=\"1.3\">4d8cea6e-3119-4e02-b98f-0d295ea1db2d</DocumentID>\n" +
            "    <CandidateSupplier>\n" +
            "        <PartyID>NAV.NO</PartyID>\n" +
            "        <PartyName>Nav</PartyName>\n" +
            "        <PersonContact>\n" +
            "            <PersonName>\n" +
            "                <oa:GivenName>Arbeidsplassen.no</oa:GivenName>\n" +
            "                <FamilyName>Arbeidsplassen.no</FamilyName>\n" +
            "            </PersonName>\n" +
            "            <Communication>\n" +
            "                <ChannelCode>Telephone</ChannelCode>\n" +
            "                <oa:DialNumber>nav.team.arbeidsplassen@nav.no</oa:DialNumber>\n" +
            "            </Communication>\n" +
            "        </PersonContact>\n" +
            "        <PrecedenceCode>1</PrecedenceCode>\n" +
            "    </CandidateSupplier>\n" +
            "    <CandidatePerson>\n" +
            "        <PersonName>\n" +
            "            <oa:GivenName>Klartenkt</oa:GivenName>\n" +
            "            <FamilyName>Fasade</FamilyName>\n" +
            "        </PersonName>\n" +
            "        <BirthDate>1998-10-21</BirthDate>\n" +
            "        <GenderCode>NotSpecified</GenderCode>\n" +
            "    </CandidatePerson>\n" +
            "    <CandidateProfile languageCode=\"no\">\n" +
            "        <ExecutiveSummary></ExecutiveSummary>\n" +
            "        <EmploymentHistory>\n" +
            "            <EmployerHistory>\n" +
            "                <OrganizationName>Oslo Universitetssykehus</OrganizationName>\n" +
            "                <EmploymentPeriod>\n" +
            "                    <StartDate>\n" +
            "                        <FormattedDateTime>2017-04-01</FormattedDateTime>\n" +
            "                    </StartDate>\n" +
            "                </EmploymentPeriod>\n" +
            "                <PositionHistory>\n" +
            "                    <PositionTitle>Anestesisykepleier</PositionTitle>\n" +
            "                    <EmploymentPeriod>\n" +
            "                        <StartDate>\n" +
            "                            <FormattedDateTime>2017-04-01</FormattedDateTime>\n" +
            "                        </StartDate>\n" +
            "                    </EmploymentPeriod>\n" +
            "                    <JobCategoryCode listName=\"ESCO_Occupations\" listURI=\"https://ec.europa.eu/esco/portal\" listSchemeURI=\"https://ec.europa.eu/esco/portal\" listVersionID=\"ESCOv1\" name=\"anestesisykepleier\">http://data.europa.eu/esco/occupation/18e14e61-495b-44cc-a7c6-df4c625934ba</JobCategoryCode>\n" +
            "                </PositionHistory>\n" +
            "            </EmployerHistory>\n" +
            "        </EmploymentHistory>\n" +
            "        <Certifications>\n" +
            "            <Certification>\n" +
            "                <CertificationName>Autorisasjon som sykepleier</CertificationName>\n" +
            "                <IssuingAuthortity>\n" +
            "                    <Name></Name>\n" +
            "                </IssuingAuthortity>\n" +
            "                <FirstIssuedDate>\n" +
            "                    <FormattedDateTime>2016-07-22</FormattedDateTime>\n" +
            "                </FirstIssuedDate>\n" +
            "                <FreeFormEffectivePeriod/>\n" +
            "            </Certification>\n" +
            "            <Certification>\n" +
            "                <CertificationName>Førstehjelpsinstruktør</CertificationName>\n" +
            "                <IssuingAuthortity>\n" +
            "                    <Name></Name>\n" +
            "                </IssuingAuthortity>\n" +
            "                <FirstIssuedDate>\n" +
            "                    <FormattedDateTime>2019-03-01</FormattedDateTime>\n" +
            "                </FirstIssuedDate>\n" +
            "                <FreeFormEffectivePeriod/>\n" +
            "            </Certification>\n" +
            "            <Certification>\n" +
            "                <CertificationName>Sveisemetode 111 - Dekkede elektroder</CertificationName>\n" +
            "                <IssuingAuthortity>\n" +
            "                    <Name></Name>\n" +
            "                </IssuingAuthortity>\n" +
            "                <FirstIssuedDate>\n" +
            "                    <FormattedDateTime>2018-04-01</FormattedDateTime>\n" +
            "                </FirstIssuedDate>\n" +
            "                <FreeFormEffectivePeriod/>\n" +
            "            </Certification>\n" +
            "            <Certification>\n" +
            "                <CertificationName>Fagbrev helsefagarbeider</CertificationName>\n" +
            "                <IssuingAuthortity>\n" +
            "                    <Name>Yrkesopplæringsnemnd</Name>\n" +
            "                </IssuingAuthortity>\n" +
            "            </Certification>\n" +
            "        </Certifications>\n" +
            "        <PersonQualifications>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/cde53fc6-f5e9-4439-8b91-a53da0c01d34</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "        </PersonQualifications>\n" +
            "    </CandidateProfile>\n" +
            "</Candidate>"



}