package no.nav.cv.eures.pdl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.AssertionErrors.assertEquals
import java.util.function.Supplier

@ActiveProfiles("test")
class PdlPersonServiceTest {

    private lateinit var pdlPersonGateway: PdlPersonService

    @MockBean
    @Qualifier("pdlTokenProvider")
    private val tokenProvider: Supplier<String> = Supplier { "value" }


    @BeforeEach
    fun setUp() {
        pdlPersonGateway = PowerMockito.spy(PdlPersonService(tokenProvider))
    }

    @Test
    fun `call get null statsborgerskap when call to pdl fails`() {
        assertEquals("Skal få null når kall feiler mot PDL",null, pdlPersonGateway.erEUEOSstatsborger("1111111"))
    }

    @Test
    fun `call erEUEOSborger returns true when has statsborgerskap NOR`() {
    val dto = HentPersonDto(HentPersonDto.HentPersonData("", HentPersonDto.HentPersonData.Person(listOf(
        HentPersonDto.HentPersonData.Person.Statsborgerskap(
            "NOR",
            null,
            null
        )
    ))))
    doReturn(dto).whenever(pdlPersonGateway).hentPersondataFraPdl(com.nhaarman.mockitokotlin2.any())

    assertEquals("Statsborgerskap NOR (Norsk) skal returnere true", true, pdlPersonGateway.erEUEOSstatsborger("11"))
    }

    @Test
    fun `call erEUEOSborger returns false when has statsborgerskap utenfor Eureslisten over godkjente land`() {
        val dto = HentPersonDto(HentPersonDto.HentPersonData("", HentPersonDto.HentPersonData.Person(listOf(
            HentPersonDto.HentPersonData.Person.Statsborgerskap(
                "AFG",
                null,
                null
            )
        ))))
        doReturn(dto).whenever(pdlPersonGateway).hentPersondataFraPdl(com.nhaarman.mockitokotlin2.any())

        assertEquals("Statsborgerskap AFG (Afganistan) skal returnere false", false, pdlPersonGateway.erEUEOSstatsborger("11"))
    }

    @Test
    fun `call erEUEOSborger returns false when godkjent land har til og med dato som er passert`() {
        val dto = HentPersonDto(HentPersonDto.HentPersonData("", HentPersonDto.HentPersonData.Person(listOf(
            HentPersonDto.HentPersonData.Person.Statsborgerskap(
                "NOR",
                null,
                "2020-01-01"
            )
        ))))
        doReturn(dto).whenever(pdlPersonGateway).hentPersondataFraPdl(com.nhaarman.mockitokotlin2.any())

        assertEquals("Utgått NOR (Norsk) statsborgerskal skal returnere false", false, pdlPersonGateway.erEUEOSstatsborger("11"))
    }

    @Test
    fun `call erEUEOSborger returns true ved flere statsborgerskap, men kun ett er gyldig`() {
        val dto = HentPersonDto(HentPersonDto.HentPersonData("", HentPersonDto.HentPersonData.Person(listOf(
            HentPersonDto.HentPersonData.Person.Statsborgerskap(
                "NOR",
                null,
                "2020-01-01"
            ), HentPersonDto.HentPersonData.Person.Statsborgerskap(
                "SWE",
                null,
                null
            )
        ))))
        doReturn(dto).whenever(pdlPersonGateway).hentPersondataFraPdl(com.nhaarman.mockitokotlin2.any())

        assertEquals("Ett gyldig statsborger av flere skal gi true", true, pdlPersonGateway.erEUEOSstatsborger("11"))
    }

    @Test
    fun `call erEUEOSborger returns false ved ingen statsborgerskap`() {
        val dto = HentPersonDto(HentPersonDto.HentPersonData("", HentPersonDto.HentPersonData.Person()))
        doReturn(dto).whenever(pdlPersonGateway).hentPersondataFraPdl(com.nhaarman.mockitokotlin2.any())

        assertEquals("Ingen statsborgerskap skal gi false", false, pdlPersonGateway.erEUEOSstatsborger("11"))
    }
}