package no.nav.cv.eures.konverterer

import io.micronaut.scheduling.annotation.Scheduled
import no.nav.cv.eures.model.*
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.apache.avro.generic.GenericRecord
import org.slf4j.LoggerFactory
import java.time.LocalDate
import javax.inject.Singleton

@Singleton
class Konverterer (
    private val cvGenericRecordRetriever: CvGenericRecordRetriever,
    private val samtykkeRepository: SamtykkeRepository
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
        val record = cvGenericRecordRetriever.getCvJavaObject(aktoerId)

        log.debug("Firstname : ${record.fornavn}")

//
//        val cvWrap = when(record.get("meldingstype")) {
//            "OPPRETT" -> record.get("opprett_cv") as GenericRecord
//            else -> record.get("opprett_cv") as GenericRecord // throw Exception("Ukjent meldingstype: " + record.get("meldingstype"))
//        }
//
//        val cv = cvWrap.get("cv") as GenericRecord
//
//        val samtykke = samtykkeRepository.hentSamtykke(aktoerId)
//                ?: throw Exception("Aktoer $aktoerId har ikke gitt samtykke")
//
//        val education = Education(cv, samtykke)
//        val employment = Employment(cv, samtykke)
//
//        val candidate = Candidate(
//                id = Id(cv.get("cv_id") as String),
//                uuid = cv.get("cv_id") as String,
//                created = Converters.timestampToLocalDateTime(cv.get("opprettet") as Long).toString(),
//                createdBy = null,
//                updated = Converters.timestampToLocalDateTime(cv.get("sist_endret") as Long).toString(),
//                updatedBy = null,
//                validFrom = LocalDate.now().toString(),
//                validTo = LocalDate.now().plusDays(3650).toString(), // about ten years
//                candidateSupplier = listOf(
//                        CandidateSupplier(
//                                partyId = "NAV.NO",
//                                partyName = "Nav",
//                                personContact = listOf(
//                                        PersonContact(
//                                                personName = Name(
//                                                        givenName = "Arbeidsplassen.no",
//                                                        familyName = "Arbeidsplassen.no"),
//                                                communication = listOf(
//                                                        Communication(
//                                                                channelCode = ChannelCode.Email,
//                                                                choice = Choice(address = null, dialNumber = null, URI = "sdfsdf"))))), // TODO ekte epostadresse
//                                precedence = 1)),
//                person = Person(
//                        personName = Name(
//                                givenName = cv.get("fornavn") as String,
//                                familyName = cv.get("etternavn") as String),
//                        communication = listOf(
//                                Communication(
//                                        channelCode = ChannelCode.MobileTelephone,
//                                        choice = Choice(address = null, dialNumber = cv.get("telefon") as String, URI = null))),
//                        residencyCountryCode = CountryCodeISO3166_Alpha_2.NO, // cv.get("land")
//                        nationality = listOf(CountryCodeISO3166_Alpha_2.NO), // cv.get("nasjonalitet")
//                        birthDate = LocalDate.of(2020, 7, 30).toString(), //cv.get("foedselsdato)
//                        gender = GenderType.NotSpecified,
//                        primaryLanguageCode = listOf(LanguageCodeISO639_1_2002_Aplpha2.Norwegian)),
//                posistionSeekingStatus = PositionSeekingStatus.Active,
//                profile = Profile(
//                        educationHistory = education.getEducationHistory(),
//                        employmentHistory = employment.getEmploymentHistory()
//                )
//        )


        log.info("AktoerID :$aktoerId")
        log.info("Record :$record")
//        log.info("Candidate :$candidate")
//        log.info("XML: ${XmlSerializer.serialize(candidate)}")
    }
}