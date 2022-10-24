package no.nav.cv.eures.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
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

        @JacksonXmlElementWrapper(useWrapping = false)
        val communication: List<Communication>

)

// 4.6.3 and 4.8
data class Name(
        @JacksonXmlProperty(localName = "oa:GivenName")
        val givenName: String,
        val familyName: String
)

// 4.6.4 and 4.9
data class Communication(
        val channelCode: ChannelCode? = null,

        val address: Address? = null,

        @JacksonXmlProperty(localName = "oa:DialNumber")
        val dialNumber: String? = null,

        @JacksonXmlProperty(localName = "oa:URI")
        val uri: String? = null

) {
    companion object {
        fun buildList(
                telephone: String? = null,
                mobileTelephone: String? = null,
                fax: String? = null,
                email: String? = null,
                instantMessage: String? = null,
                web: String? = null,
                address: String? = null,
                zipCode: String? = null,
                city: String? = null,
                countryCode: String? = null
        ) : List<Communication> {
            val comList = mutableListOf<Communication>()

            if(telephone != null)
                comList.add(
                        Communication(
                                channelCode = ChannelCode.Telephone,
                                dialNumber = telephone
                        ))

            if(mobileTelephone != null)
                comList.add(
                        Communication(
                                channelCode = ChannelCode.MobileTelephone,
                                dialNumber = mobileTelephone
                        ))

            if(fax != null)
                comList.add(
                        Communication(
                                channelCode = ChannelCode.Fax,
                                dialNumber = fax
                        ))

            if(email != null)
                comList.add(
                        Communication(
                                channelCode = ChannelCode.Email,
                                uri = email
                        ))

            if(instantMessage != null)
                comList.add(
                        Communication(
                                channelCode = ChannelCode.InstantMessage,
                                uri = instantMessage
                        ))

            if(web != null)
                comList.add(
                        Communication(
                                channelCode = ChannelCode.Web,
                                uri = web
                        ))

            if(address != null && zipCode != null && city != null && countryCode != null) {

                // This is not implemented in the EURES XML specification at this stage (only in the word doc)
                val addressLine = null//"$address, $zipCode $city, $countryCode"

                comList.add(
                        Communication(
                                address = Address(
                                        cityName = city,
                                        countryCode = countryCode,
                                        postalCode = zipCode,
                                        addressLine = addressLine)
                        )
                )
            }

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

// 4.9.4
data class Address(
        @JacksonXmlProperty(isAttribute = true, localName = "currentAddressIndicator")
        val currentAddressIndicator: String = "true",

        @JacksonXmlProperty(isAttribute = true, localName = "type")
        val type: String = "Residence",

        @JacksonXmlProperty(localName = "oa:CityName")
        val cityName: String,

        val countryCode: String,

        @JacksonXmlProperty(localName = "oa:PostalCode")
        val postalCode: String,

        // This is not implemented in the EURES XML specification at this stage (only in the word doc)
        @JacksonXmlProperty(localName = "oa:AddressLine")
        val addressLine: String? = null
)
