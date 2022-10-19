package no.nav.cv.eures.konverterer.country


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader


/**
 *
 * Nationality mapping file fetched from NAV Kodeverk
 * URL: https://kodeverk-web.dev.adeo.no/kodeverksoversikt/kodeverk/LandkoderISO2
 * First fetch: 2020.11.10 10:15 CET
 * Updated 2021.06.21 14:00 CEST with new country names from the same source.
 *
 * Suggested merge : diff -w --changed-group-format='%>' --unchanged-group-format='' landkoder.csv updated_landkoder.csv
 * And then paste the result at the end of the landkoder file.
 *
 * Eures wants ISO 3166-1 Alpha-2 based nationality codes
 */

object NationalityConverter {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    private val filename = "landkoder.csv"

    private val isoToString: Map<String, String>
    private val stringToIso: Map<String, String>

    init {
        val i2s = mutableMapOf<String, String>()
        val s2i = mutableMapOf<String, String>()

        // Some hardcoded fixes
        i2s["SF"] = "Finland".uppercase() // Listed in Wikipedia as reserved for Finland until 2012, but we still have records with this code
        s2i["NORSK"] = "NO" // pam-cv-api produces kafka test messages with 'Norsk' instead of 'Norge' in the nationality field

        /* Not accepted by EURES at this stage
        i2s["XX"] = "STATSLØS" // Stateless persons
        s2i["STATSLØS"] = "XX" // According to https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3 (2020.11.10)
        */


        val inputStream = NationalityConverter::class.java.classLoader.getResourceAsStream(filename)

        val reader = BufferedReader(InputStreamReader(inputStream!!))

        for(line in reader.readLines()) {
            val columns = line.split(";")

            val iso = columns[0].uppercase()
            val str = columns[1].uppercase()

            // The latest versions of country names are in the end of the landskoder.csv file, so they'll overwrite the
            // earlier versions

            i2s[iso] = str
            s2i[str] = iso
        }

        isoToString = i2s
        stringToIso = s2i
    }

    fun getIsoCode(nationality: String) : String? {
        val nationalityUpper = nationality.uppercase()

        if(isoToString.containsKey(nationalityUpper))
            return nationalityUpper

        return stringToIso[nationalityUpper]
                ?: run {log.error("Unknown nationality $nationality when mapping to ISO code"); null}
    }
}