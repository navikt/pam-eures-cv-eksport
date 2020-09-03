package no.nav.cv.eures.model

import no.nav.cv.eures.samtykke.Samtykke
import org.apache.avro.generic.GenericRecord


class Contact(
        private val cv: GenericRecord,
        private val samtykke: Samtykke
) {
    fun getCommunicationList() : List<Communication> {


        return listOf()

    }
}

// 4.6
data class PersonContact(
        val personName: Name,
        val communication: List<Communication>

)

// 4.6.3 and 4.8
data class Name(
        val givenName: String,
        val familyName: String
)

// 4.6.4 and 4.9
data class Communication(
        val channelCode: ChannelCode,
        val choice: Choice
)

// 4.28.3
enum class ChannelCode{
    Telephone,
    MobileTelephone,
    Fax,
    Email,
    InstantMessage,
    Web
}

// 4.6.5 and 4.9.3
data class Choice(
        val address: Address?,
        val dialNumber: String?,
        val URI: String?
)

// 4.9.4
data class Address(
        val cityName: String,
        val countryCode: CountryCodeISO3166_Alpha_2,
        val postalCode: PostalCode
)

// EURES_PostalCodes.gc  NUTS 2013
enum class PostalCode {}
