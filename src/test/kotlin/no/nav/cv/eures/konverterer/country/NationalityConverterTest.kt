package no.nav.cv.eures.konverterer.country

import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@MicronautTest
class NationalityConverterTest {

    @Test
    fun `all nationality records in database - maps to ISO code`() {
        extractedFromDatabase10Nov2020.forEach { record ->
            val iso = NationalityConverter.getIsoCode(record)

            assertNotNull(iso)
        }
    }

    private val extractedFromDatabase10Nov2020 = listOf(
        "SD",
        "US",
        "SG",
        "LT",
        "ER",
        "TR",
        "SN",
        "AF",
        "BY",
        "FJ",
        "JM",
        "NZ",
        "Somalia",
        "GD",
        "Kenya",
        "SI",
        "MN",
        "Sudan",
        "MG",
        "MW",
        "Serbia",
        "GQ",
        "PS",
        "Kosovo",
        "Hviterussland",
        "Slovakia",
        "Algerie",
        "Zimbabwe",
        "Zambia",
        "Elfenbeinskysten",
        "Swaziland",
        "Oman",
        "NG",
        "ES",
        "PH",
        "PK",
        "IS",
        "CA",
        "CH",
        "RS",
        "KE",
        "CZ",
        "AL",
        "BO",
        "GE",
        "SU",
        "Brasil",
        //"XY", // Don't know how to handle these yet. Might be old entries from Arena, but cannot check due to Utvikler Image being down
        "EG",
        "LR",
        "Syria",
        "BF",
        "HT",
        "Russland",
        "AE",
        "Kroatia",
        "Ukraina",
        "Republikken Kina",
        "Chile",
        "Peru",
        "Usa",
        "Frankrike",
        "Jordan",
        "Moldova",
        "Liberia",
        "Malaysia",
        "Den Dominikanske Rep",
        "Østerrike",
        "Jamaica",
        "Sveits",
        "Kirgisistan",
        "Kypros",
        "Kamerun",
        "Botswana",
        "Malawi",
        "Paraguay",
        "Madagaskar",
        "Togo",
        "Mauritania",
        "Guyana",
        "MX",
        "FR",
        "SO",
        "IQ",
        "IT",
        "BE",
        "AR",
        "MA",
        "TD",
        "VN",
        "NP",
        "PE",
        "EE",
        "DZ",
        "NI",
        "TG",
        "Sverige",
        "BT",
        "MZ",
        "AM",
        "KG",
        "GN",
        "CF",
        "LU",
        "Spania",
        "BB",
        "Tyrkia",
        "Uganda",
        "Kongo, Den Demokr. Republ",
        "Pakistan",
        "Nepal",
        "New Zealand",
        "Trinidad Og Tobago",
        "Armenia",
        "Sierra Leone",
        "Slovenia",
        "Mongolia",
        "Serbia Og Montenegro",
        "Montenegro",
        "Azerbajdzjan",
        "Mali",
        "Barbados",
        "De Arabiske Emirater",
        "Maldivene",
        "Guinea-Bissau",
        "FI",
        "HR",
        "SY",
        "TN",
        "MY",
        "CS",
        "JO",
        "GR",
        "KR",
        "CY",
        "TZ",
        "YE",
        "NA",
        "MD",
        "KH",
        "NE",
        "Storbritannia",
        "LC",
        "MR",
        "TJ",
        "GT",
        "India",
        "KW",
        "Island",
        "Hellas",
        "Makedonia",
        "Bangladesh",
        "Tadzjikistan",
        "Finland",
        "Irland",
        "Kazakhstan",
        "Nigeria",
        "El Salvador",
        "Libanon",
        "Guatemala",
        "Honduras",
        "Mosambik",
        "Senegal",
        "Venezuela",
        "Belize",
        "Saudi-Arabia",
        "Nord-Korea",
        "Kapp Verde",
        "St. Lucia",
        "NO",
        "SE",
        "DK",
        "CL",
        "LV",
        "CD",
        "PT",
        // "XX", // Not accepted by EURES at this stage
        "IE",
        "GY",
        "ZA",
        "LB",
        "IL",
        "HU",
        "CG",
        "ZM",
        "MM",
        "TT",
        "ZW",
        "ME",
        "DM",
        "Latvia",
        "Romania",
        "GA",
        "BZ",
        "Etiopia",
        "Afghanistan",
        "Nederland",
        "Ungarn",
        "Australia",
        "Vietnam",
        "Jemen",
        "Bhutan",
        "Egypt",
        "Myanmar (Burma)",
        "Ecuador",
        "Angola",
        "Tanzania",
        "Gambia",
        "Cuba",
        "Gabon",
        "Costa Rica",
        "Bolivia",
        "Malta",
        "Laos",
        "Djibouti",
        "Luxembourg",
        "GB",
        "BA",
        "BG",
        "DE",
        "RU",
        "GM",
        "MK",
        "YU",
        "AT",
        "RO",
        "Norge",
        "CO",
        "CN",
        "CM",
        "UZ",
        "UG",
        "UY",
        "MT",
        "SV",
        "CV",
        "BJ",
        "Tyskland",
        "Burundi",
        "Belgia",
        "Canada",
        "Mauritius",
        "Den Tsjekkiske Rep.",
        "Albania",
        "Indonesia",
        "Uzbekistan",
        "Israel",
        "Sør-Korea",
        "Rwanda",
        "Benin",
        "Salomonøyene",
        "PL",
        "SA",
        "TH",
        "AU",
        "IN",
        "BR",
        "JP",
        "BD",
        "XK",
        "CU",
        "ID",
        "UA",
        "SL",
        "BI",
        "DO",
        "Eritrea",
        "VC",
        "TM",
        "SF",
        "BW",
        "LA",
        "CI",
        "PY",
        "HN",
        "Polen",
        "BS",
        "Litauen",
        "SS",
        "Bulgaria",
        "Irak",
        "BN",
        "Danmark",
        "Italia",
        "Sør-Afrika",
        "Argentina",
        "Singapore",
        "Kambodsja",
        "Libya",
        "Namibia",
        "Guinea",
        "Kongo, Republikken",
        "Sør-Sudan",
        "Turkmenistan",
        "Bahamas",
        "LK",
        "SK",
        "NL",
        "IR",
        "ET",
        "EC",
        "DJ",
        "VE",
        "GH",
        "AZ",
        "RW",
        "ML",
        "CR",
        "AO",
        "GW",
        "KZ",
        "LY",
        "PA",
        "MU",
        "AG",
        "Iran",
        "Filippinene",
        "Panama",
        "Bosnia-Hercegovina",
        "Estland",
        //"Statsløs", // Maps to XX but not accepted by Eures at this stage
        "Thailand",
        "Portugal",
        "Colombia",
        "Marokko",
        "Ghana",
        "Japan",
        "Georgia",
        "Sri Lanka",
        "Tunisia",
        "Mexico",
        "Uruguay",
        "Nicaragua",
        "Sentralafrika. Rep.",
        "Haiti",
        "Bahrain",
        "Samoa")
}