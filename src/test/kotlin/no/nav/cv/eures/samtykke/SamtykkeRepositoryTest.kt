package no.nav.cv.eures.samtykke

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@DataJpaTest(includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [Repository::class])])
@EnableMockOAuth2Server
internal class SamtykkeRepositoryTest {
    val foedselsnummer = "dummy"
    val foedselsnummer2 = "dummy2"
    val now: ZonedDateTime = ZonedDateTime.now()
    val yesterday: ZonedDateTime = ZonedDateTime.now().minusDays(1)

    @Autowired
    lateinit var samtykkeRepository: SamtykkeRepository


    @Test
    fun `lagre og hente samtykke`() {
        val samtykke = Samtykke(now, personalia = true, utdanning = false, kompetanser = true)
        samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykke)

        val hentet = samtykkeRepository.hentSamtykke(foedselsnummer)

        assertEquals(samtykke.sistEndret, hentet?.sistEndret)
        assertEquals(samtykke.personalia, hentet?.personalia)
        assertEquals(samtykke.utdanning, hentet?.utdanning)
        assertEquals(samtykke.kompetanser, hentet?.kompetanser)
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
        val samtykke = Samtykke(yesterday, personalia = true, utdanning = false, offentligeGodkjenninger = true)
        samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykke)

        val hentet = samtykkeRepository.hentSamtykke(foedselsnummer)

        assertEquals(samtykke.sistEndret, hentet?.sistEndret)
        assertEquals(samtykke.personalia, hentet?.personalia)
        assertEquals(samtykke.utdanning, hentet?.utdanning)
        assertEquals(samtykke.offentligeGodkjenninger, hentet?.offentligeGodkjenninger)

        val samtykkeOppdatert = Samtykke(now, personalia = false, utdanning = true, offentligeGodkjenninger = false)
        samtykkeRepository.oppdaterSamtykke(foedselsnummer, samtykkeOppdatert)

        val hentetOppdatert  = samtykkeRepository.hentSamtykke(foedselsnummer)

        assertEquals(samtykkeOppdatert.sistEndret, hentetOppdatert?.sistEndret)
        assertEquals(samtykkeOppdatert.personalia, hentetOppdatert?.personalia)
        assertEquals(samtykkeOppdatert.utdanning, hentetOppdatert?.utdanning)
        assertEquals(samtykke.offentligeGodkjenninger, hentet?.offentligeGodkjenninger)

    }
}