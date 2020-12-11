package no.nav.cv.eures.konverterer.language

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


/**
 *
 * Language mapping file fetched from SIL.org
 * URL: https://iso639-3.sil.org/sites/iso639-3/files/downloads/iso-639-3.tab
 * Date: 2020.11.09 09:40 CET
 *
 */


object LanguageConverter {
    private val filename = "iso-639-3.tab.txt"

    private val iso3ToIso2Map by lazy { loadLanguages() }

    fun fromIso3ToIso1(iso3: String) = iso3ToIso2Map[iso3]

    private fun loadLanguages() : Map<String, String> {
        val mapping = mutableMapOf<String, String>()

        val inputStream = LanguageConverter::class.java.classLoader.getResourceAsStream(filename)

        val reader = BufferedReader(InputStreamReader(inputStream!!))

        val lines = reader.readLines()

        for(line in lines.subList(1, lines.size)) {
            val columns = line.split("\t")

            val iso3 = columns[0]
            val iso1 = columns[3]

            if(iso1.isNotBlank())
                mapping[iso3] = iso1
        }

        return mapping
    }

}