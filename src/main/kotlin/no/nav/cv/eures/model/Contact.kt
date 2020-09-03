package no.nav.cv.eures.model

import no.nav.arbeid.cv.avro.Cv
import no.nav.cv.eures.samtykke.Samtykke


class Contact(
        private val cv: Cv,
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
) {
    companion object {
        fun buildList(
                telephone: String? = null,
                mobileTelephone: String? = null,
                fax: String? = null,
                email: String? = null,
                instantMessage: String? = null,
                web: String? = null
        ) : List<Communication> {
            val comList = mutableListOf<Communication>()

            if(telephone != null)
                comList.add(
                        Communication(
                                ChannelCode.Telephone,
                                Choice(dialNumber = telephone)
                        ))


            if(mobileTelephone != null)
                comList.add(
                        Communication(
                                ChannelCode.MobileTelephone,
                                Choice(dialNumber = mobileTelephone)
                        ))


            if(fax != null)
                comList.add(
                        Communication(
                                ChannelCode.Fax,
                                Choice(dialNumber = fax)
                        ))


            if(email != null)
                comList.add(
                        Communication(
                                ChannelCode.Email,
                                Choice(URI = email)
                        ))


            if(instantMessage != null)
                comList.add(
                        Communication(
                                ChannelCode.InstantMessage,
                                Choice(URI = instantMessage)
                        ))


            if(web != null)
                comList.add(
                        Communication(
                                ChannelCode.Web,
                                Choice(URI = web)
                        ))

            return comList
        }
    }
}

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
        val address: Address? = null,
        val dialNumber: String? = null,
        val URI: String? = null
)

// 4.9.4
data class Address(
        val cityName: String,
        val countryCode: CountryCodeISO3166_Alpha_2,
        val postalCode: PostalCode
)

// EURES_PostalCodes.gc  NUTS 2013
enum class PostalCode {}
