package no.nav.cv.eures.konverterer

import no.nav.cv.eures.cv.CvXml
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime

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

    @Test
    fun `test test`() {


    }

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
            "                <CompetencyID>NO HIT</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>NO HIT</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/cde53fc6-f5e9-4439-8b91-a53da0c01d34</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>NO HIT</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>NO HIT</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/468e27bb-d5fa-4062-9d22-97e0d7546717</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/468e27bb-d5fa-4062-9d22-97e0d7546717</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/bec4359e-cb92-468f-a997-8fb28e32fba9</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/bec4359e-cb92-468f-a997-8fb28e32fba9</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>NO HIT</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>NO HIT</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/0cb013af-6dd9-4fb2-b34e-63dda1a5cf82</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/279101c1-e325-4349-bf58-401b6c8ff208</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/0cb013af-6dd9-4fb2-b34e-63dda1a5cf82</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/279101c1-e325-4349-bf58-401b6c8ff208</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/7e9daad3-8604-4839-af9b-dc6f8db3ef68</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/7e9daad3-8604-4839-af9b-dc6f8db3ef68</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/28722c99-68b7-4b7b-808f-53f343299935</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/S1.5</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/28722c99-68b7-4b7b-808f-53f343299935</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/S1.5</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/84a67aa9-1d83-4122-a381-cef8c770c8db</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/84a67aa9-1d83-4122-a381-cef8c770c8db</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/5f1dd9f6-dcb4-4384-88c9-f9f6a75305c9</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/64e09849-a7db-4d6b-a932-66264420eb97</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/67dbfc2a-3dd4-41a1-90bd-97035e7add60</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/5f1dd9f6-dcb4-4384-88c9-f9f6a75305c9</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/64e09849-a7db-4d6b-a932-66264420eb97</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/67dbfc2a-3dd4-41a1-90bd-97035e7add60</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/09e28145-e205-4b7a-8b3b-5c4876396069</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/09e28145-e205-4b7a-8b3b-5c4876396069</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>NO HIT</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>NO HIT</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>NO HIT</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>NO HIT</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/30385f84-3f06-4f5b-b93f-e34997013a8b</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/30385f84-3f06-4f5b-b93f-e34997013a8b</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/0359f76c-3ef3-4e52-abcf-1b2b32ed20a5</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/134690b7-6e3f-4fa8-829c-2be7034d0617</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/13bf8e76-044a-43c3-9924-ee6d99b50a9f</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/343dd8e6-225f-460b-99e4-2e24ecbfd7ed</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/3c927804-beb3-4351-bb70-21bfdedc62c5</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/0359f76c-3ef3-4e52-abcf-1b2b32ed20a5</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/134690b7-6e3f-4fa8-829c-2be7034d0617</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/13bf8e76-044a-43c3-9924-ee6d99b50a9f</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/343dd8e6-225f-460b-99e4-2e24ecbfd7ed</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "            <PersonCompetency>\n" +
            "                <CompetencyID>http://data.europa.eu/esco/skill/3c927804-beb3-4351-bb70-21bfdedc62c5</CompetencyID>\n" +
            "                <TaxonomyID>other</TaxonomyID>\n" +
            "            </PersonCompetency>\n" +
            "        </PersonQualifications>\n" +
            "    </CandidateProfile>\n" +
            "</Candidate>"

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