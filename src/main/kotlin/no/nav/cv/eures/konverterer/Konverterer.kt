package no.nav.cv.eures.konverterer

import io.micronaut.scheduling.annotation.Scheduled
import no.nav.cv.eures.model.*
import org.apache.avro.generic.GenericRecord
import org.slf4j.LoggerFactory
import java.time.LocalDate
import javax.inject.Singleton

@Singleton
class Konverterer (
    private val cvGenericRecordRetriever: CvGenericRecordRetriever
) {

    companion object {
        val log = LoggerFactory.getLogger(Konverterer::class.java)
    }

    fun oppdater(aktoerId: String) {

    }

    fun konverterTilXML(aktoerId: String) : String {




       return "Nothing"
    }

    @Scheduled(fixedDelay = "5s")
    fun testing() {

        val aktoerId = "10013106889"
        val record = cvGenericRecordRetriever.getCvGenericRecord(aktoerId)

        val cvWrap = when(record.get("meldingstype")) {
            "OPPRETT" -> record.get("opprett_cv") as GenericRecord
            else -> record.get("opprett_cv") as GenericRecord // throw Exception("Ukjent meldingstype: " + record.get("meldingstype"))
        }

        val cv = cvWrap.get("cv") as GenericRecord

        val candidate = Candidate(
                id = Id(cv.get("cv_id") as String),
                uuid = cv.get("cv_id") as String,
                created = Converters.timestampToLocalDateTime(cv.get("opprettet") as Long).toString(),
                createdBy = null,
                updated = Converters.timestampToLocalDateTime(cv.get("sist_endret") as Long).toString(),
                updatedBy = null,
                validFrom = LocalDate.now().toString(),
                validTo = LocalDate.now().plusDays(3650).toString(), // about ten years
                candidateSupplier = listOf(
                        CandidateSupplier(
                                id = "NAV.NO",
                                partyName = "Nav",
                                personContact = listOf(
                                        PersonContact(
                                                personName = Name(
                                                        givenName = "Magnus Skjeggerud",
                                                        familyName = "Espeland"),
                                                communication = listOf(
                                                        Communication(
                                                                channelCode = ChannelCode.Email,
                                                                choice =  Choice(address = null, dialNumber = null, URI = "magnus.s.espeland@nav.no"))))),
                                precedence = 1)),
                person = Person(
                        personName = Name(
                                givenName = cv.get("fornavn") as String,
                                familyName = cv.get("etternavn") as String),
                        communication = listOf(
                                Communication(
                                        channelCode = ChannelCode.MobileTelephone,
                                        choice = Choice(address = null, dialNumber = cv.get("telefon") as String, URI = null))),
                        residencyCountryCode = CountryCodeISO3166_Alpha_2.NO, // cv.get("land")
                        nationality = listOf(CountryCodeISO3166_Alpha_2.NO), // cv.get("nasjonalitet")
                        birthDate = LocalDate.of(2020, 7, 30).toString(), //cv.get("foedselsdato)
                        gender = GenderType.NotSpecified,
                        primaryLanguageCode = listOf(LanguageCodeISO639_1_2002_Aplpha2.Norwegian)),
                posistionSeekingStatus = PositionSeekingStatus.Active,
                profile = Profile(
                    educationHistory = EducationHistory(
                            listOf(OrganizationAttendance(
                                    organizationName = "Test skole",
                                    educationLevel = EducationLevel.Bachelor,
                                    attendancePeriod = AttendancePeriod(LocalDate.of(2010, 8, 1).toString(), null)
                            ))
                    )
                )
        )


        log.info("AktoerID :$aktoerId")
        log.info("Record :$record")
        log.info("Candidate :$candidate")
        log.info("XML: ${XmlSerializer.serialize(candidate)}")
    }
}