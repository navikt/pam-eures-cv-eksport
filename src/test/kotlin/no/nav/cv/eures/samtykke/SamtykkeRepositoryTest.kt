package no.nav.cv.eures.samtykke

import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest
internal class SamtykkeRepositoryTest {
    val aktoerId1 = "dummy"
    val aktoerId2 = "dummy2"
    val now = ZonedDateTime.now()
    val yesterday = ZonedDateTime.now().minusDays(1)

    @Inject
    lateinit var samtykkeRepository: SamtykkeRepository


    @Test
    fun `lagre og hente samtykke`() {
        val samtykke = Samtykke(aktoerId1, now, true, false)
        samtykkeRepository.oppdaterSamtykke(samtykke)

        val hentet = samtykkeRepository.hentSamtykke(aktoerId1)

        assertEquals(samtykke.foedselsnummer, hentet?.foedselsnummer)
        assertEquals(samtykke.sistEndret, hentet?.sistEndret)
        assertEquals(samtykke.personalia, hentet?.personalia)
        assertEquals(samtykke.utdanning, hentet?.utdanning)
    }

    @Test
    fun `lagre, slette og hente samtykke - bare slette en av to`() {
        val samtykke1 = Samtykke(aktoerId1, now, true, true)
        val samtykke2 = Samtykke(aktoerId2, yesterday, false, false)

        samtykkeRepository.oppdaterSamtykke(samtykke1)
        samtykkeRepository.oppdaterSamtykke(samtykke2)

        samtykkeRepository.slettSamtykke(aktoerId1)

        val hentet1 = samtykkeRepository.hentSamtykke(aktoerId1)
        val hentet2 = samtykkeRepository.hentSamtykke(aktoerId2)

        assertNull(hentet1)

        assertEquals(samtykke2.foedselsnummer, hentet2?.foedselsnummer)
        assertEquals(samtykke2.sistEndret, hentet2?.sistEndret)
        assertEquals(samtykke2.personalia, hentet2?.personalia)
        assertEquals(samtykke2.utdanning, hentet2?.utdanning)
    }

    @Test
    fun `oppdater samtykke - to ganger`() {
        val samtykke = Samtykke(aktoerId1, yesterday, true, false)
        samtykkeRepository.oppdaterSamtykke(samtykke)

        val hentet = samtykkeRepository.hentSamtykke(aktoerId1)

        assertEquals(samtykke.foedselsnummer, hentet?.foedselsnummer)
        assertEquals(samtykke.sistEndret, hentet?.sistEndret)
        assertEquals(samtykke.personalia, hentet?.personalia)
        assertEquals(samtykke.utdanning, hentet?.utdanning)

        val samtykkeOppdatert = Samtykke(aktoerId1, now, false, true)
        samtykkeRepository.oppdaterSamtykke(samtykkeOppdatert)

        val hentetOppdatert  = samtykkeRepository.hentSamtykke(aktoerId1)

        assertEquals(samtykkeOppdatert.foedselsnummer, hentetOppdatert?.foedselsnummer)
        assertEquals(samtykkeOppdatert.sistEndret, hentetOppdatert?.sistEndret)
        assertEquals(samtykkeOppdatert.personalia, hentetOppdatert?.personalia)
        assertEquals(samtykkeOppdatert.utdanning, hentetOppdatert?.utdanning)
    }
}