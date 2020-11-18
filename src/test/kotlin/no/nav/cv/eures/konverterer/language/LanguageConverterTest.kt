package no.nav.cv.eures.konverterer.language

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class LanguageConverterTest() {

    private val iso3Toiso1Mappings = mapOf(
            "aar" to "aa", // Afar
            "nob" to "nb", // Norwegian Bokmål
            "non" to null, // Old Norse
            "zul" to "zu", // Zulu
            "zza" to null, // Zaza (has ISO2 mapping)
            "zzj" to null  // Zuojiang Zhuang (no ISO2 mapping)
    )

    @Test
    fun `test mapping iso3 to iso1`() {
        for((iso3, correctMapping) in iso3Toiso1Mappings) {
            val givenMapping = LanguageConverter.fromIso3ToIso1(iso3)

            assertEquals(correctMapping, givenMapping)
        }
    }

}