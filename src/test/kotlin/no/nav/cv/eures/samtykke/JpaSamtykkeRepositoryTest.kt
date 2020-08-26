package no.nav.cv.eures.samtykke

import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest
internal class JpaSamtykkeRepositoryTest {
    val aktoer = "dummy"
    val now = ZonedDateTime.now()

    @Inject
    lateinit var samtykkeRepository: SamtykkeRepository


    @Test
    fun `lagre og hente samtykke`() {
        val samtykke = Samtykke(aktoer, now, true, false)
        samtykkeRepository.oppdaterSamtykke(samtykke)

        val hentet = samtykkeRepository.hentSamtykke(aktoer)

        assertEquals(samtykke.aktoerId, hentet?.aktoerId)
        assertEquals(samtykke.sistEndret, hentet?.sistEndret)
        assertEquals(samtykke.personalia, hentet?.personalia)
        assertEquals(samtykke.utdanning, hentet?.utdanning)
    }

    @Test
    fun `lagre, slette og hente samtykke`() {
        val samtykke = Samtykke(aktoer, now, true, true)
        samtykkeRepository.oppdaterSamtykke(samtykke)
        samtykkeRepository.slettSamtykke(samtykke)

        val hentet = samtykkeRepository.hentSamtykke(aktoer)

        assertEquals(samtykke.aktoerId, hentet?.aktoerId)
    }

    @Test
    fun `oppdater samtykke`() {
    }
}