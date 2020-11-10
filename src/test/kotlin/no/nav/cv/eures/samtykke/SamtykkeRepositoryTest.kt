package no.nav.cv.eures.samtykke

import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest
internal class SamtykkeRepositoryTest {
    val foedselsnummer = "dummy"
    val foedselsnummer2 = "dummy2"
    val now = ZonedDateTime.now()
    val yesterday = ZonedDateTime.now().minusDays(1)

    @Inject
    lateinit var samtykkeRepository: SamtykkeRepository


    @Test
    fun `lagre og hente samtykke`() {
        val samtykke = Samtykke(now, personalia = true, utdanning = false)
        samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykke)

        val hentet = samtykkeRepository.hentSamtykke(foedselsnummer)

        assertEquals(samtykke.sistEndret, hentet?.sistEndret)
        assertEquals(samtykke.personalia, hentet?.personalia)
        assertEquals(samtykke.utdanning, hentet?.utdanning)
    }

    @Test
    fun `lagre, slette og hente samtykke - bare slette en av to`() {
        val samtykke1 = Samtykke(now, personalia = true, utdanning = true)
        val samtykke2 = Samtykke(yesterday, personalia = false, utdanning = false)

        samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykke1)
        samtykkeRepository.oppdaterSamtykke(foedselsnummer2, samtykke2)

        samtykkeRepository.slettSamtykke(foedselsnummer)

        val hentet1 = samtykkeRepository.hentSamtykke(foedselsnummer)
        val hentet2 = samtykkeRepository.hentSamtykke(foedselsnummer2)

        assertNull(hentet1)

        assertEquals(samtykke2.sistEndret, hentet2?.sistEndret)
        assertEquals(samtykke2.personalia, hentet2?.personalia)
        assertEquals(samtykke2.utdanning, hentet2?.utdanning)
    }

    @Test
    fun `oppdater samtykke - to ganger`() {
        val samtykke = Samtykke(yesterday, personalia = true, utdanning = false)
        samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykke)

        val hentet = samtykkeRepository.hentSamtykke(foedselsnummer)

        assertEquals(samtykke.sistEndret, hentet?.sistEndret)
        assertEquals(samtykke.personalia, hentet?.personalia)
        assertEquals(samtykke.utdanning, hentet?.utdanning)

        val samtykkeOppdatert = Samtykke(now, personalia = false, utdanning = true)
        samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykkeOppdatert)

        val hentetOppdatert  = samtykkeRepository.hentSamtykke(foedselsnummer)

        assertEquals(samtykkeOppdatert.sistEndret, hentetOppdatert?.sistEndret)
        assertEquals(samtykkeOppdatert.personalia, hentetOppdatert?.personalia)
        assertEquals(samtykkeOppdatert.utdanning, hentetOppdatert?.utdanning)
    }
}