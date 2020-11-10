package no.nav.cv.eures.konverterer.country


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File


/**
 *
 * Nationality mapping file fetched from NAV Kodeverk
 * URL: https://kodeverk-web.dev.adeo.no/kodeverksoversikt/kodeverk/LandkoderISO2
 * Date: 2020.11.10 10:15 CET
 *
 * Eures wants ISO 3166-1 Alpha-2 based nationality codes
 */

object NationalityConverter {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    private val filename = "converting/landkoder.csv"

    private val isoToString: Map<String, String>
    private val stringToIso: Map<String, String>

    init {
        val i2s = mutableMapOf<String, String>()
        val s2i = mutableMapOf<String, String>()

        // Some hardcoded fixes
        i2s["SF"] = "Finland".toUpperCase() // Listed in Wikipedia as reserved for Finland until 2012, but we still have records with this code

        i2s["XX"] = "STATSLØS" // Stateless persons
        s2i["STATSLØS"] = "XX" // According to https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3 (2020.11.10)

        s2i["NORSK"] = "NO" // pam-cv-api produces kafka test messages with 'Norsk' instead of 'Norway' in the nationality field

        val uri = javaClass.classLoader.getResource(filename)?.toURI()
                ?: throw Exception("File '$filename' gives null")

        val file = File(uri)

        for(line in file.readLines()) {
            val columns = line.split(";")

            val iso = columns[0].toUpperCase()
            val str = columns[1].toUpperCase()

            if(i2s.containsKey(iso))
                throw Exception("Attempting to overwrite iso code $iso. Was: ${i2s[iso]} Now: $str")

            if(s2i.containsKey(str))
                throw Exception("Attempting to overwrite string code $str. Was: ${s2i[str]} Now: $iso")

            i2s[iso] = str
            s2i[str] = iso
        }

        isoToString = i2s
        stringToIso = s2i
    }

    fun getIsoCode(nationality: String) : String? {
        val nationalityUpper = nationality.toUpperCase()

        if(isoToString.containsKey(nationalityUpper))
            return nationalityUpper

        return stringToIso[nationalityUpper]
                ?: run {log.error("Unknown nationality $nationality when mapping to ISO code"); null}
    }
}