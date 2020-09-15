package no.nav.cv.eures.konverterer

import io.micronaut.scheduling.annotation.Scheduled
import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.cv.eures.model.*
import no.nav.cv.eures.samtykke.Samtykke
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Singleton

@Singleton
class Konverterer (
        private val cvRecordRetriever: CvRecordRetriever,
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
        val record = cvRecordRetriever.getCvDTO(aktoerId)


        val cv = when(record.meldingstype) {
            Meldingstype.OPPRETT -> record.opprettCv.cv
            Meldingstype.ENDRE -> record.endreCv.cv
            else -> throw Exception("Ukjent meldingstype: " + record.get("meldingstype"))
        }

        log.debug("Firstname : ${cv.fornavn}")


        val samtykke = samtykkeRepository.hentSamtykke(aktoerId)
                ?: Samtykke(aktoerId = aktoerId, sistEndret = ZonedDateTime.now(), utdanning = true)
                ?: throw Exception("Aktoer $aktoerId har ikke gitt samtykke")

        val education = Education(cv, samtykke)
        val employment = Employment(cv, samtykke)

        val candidate = Candidate(
                documentId = DocumentId(uuid = cv.cvId),
//                created = cv.opprettet.toString(),
//                createdBy = null,
//                updated = cv.sistEndret.toString(),
//                updatedBy = null,
                validFrom = LocalDate.now().toString(),
                validTo = LocalDate.now().plusDays(3650).toString(), // about ten years
                candidateSupplier = listOf(
                        CandidateSupplier(
                                partyId = "NAV.NO",
                                partyName = "Nav",
                                personContact = listOf(
                                        PersonContact(
                                                personName = Name(
                                                        givenName = "Arbeidsplassen.no",
                                                        familyName = "Arbeidsplassen.no"),
                                                communication = Communication.buildList(telephone = "nav.team.arbeidsplassen@nav.no"))), // TODO ekte epostadresse
                                precedenceCode = 1)),
                candidatePerson =  CandidatePerson(
                        personName = Name(
                                givenName = cv.fornavn,
                                familyName = cv.etternavn),
                        communication = Communication.buildList(telephone = cv.telefon, mobileTelephone = cv.epost),
                        residencyCountryCode = CountryCodeISO3166_Alpha_2.NO, // cv.get("land")
                        nationalityCode = listOf(CountryCodeISO3166_Alpha_2.NO), // cv.get("nasjonalitet")
                        birthDate = cv.foedselsdato.toString(),
                        genderCode = GenderCode.NotSpecified,
                        primaryLanguageCode = listOf(LanguageCodeISO639_1_2002_Aplpha2.NB)),
                //posistionSeekingStatus = PositionSeekingStatus.Active,
                candidateProfile = CandidateProfile(
                        educationHistory = education.getEducationHistory()
                        //employmentHistory = employment.getEmploymentHistory()
                )
        )


        log.info("AktoerID :$aktoerId")
        log.info("Record :$record")
        log.info("Candidate :$candidate")
        log.info("XML: ${XmlSerializer.serialize(candidate)}")
    }
}