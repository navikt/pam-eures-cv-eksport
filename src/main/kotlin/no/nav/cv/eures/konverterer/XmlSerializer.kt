package no.nav.cv.eures.konverterer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StringSerializer
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object XmlSerializer {
    private val stringSerializer = StringSerializer()
    private val escapeRegex =
            Regex("[^\u0009\u000A\u000D\u0020-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]")
    private val xml: XmlMapper = XmlMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        propertyNamingStrategy = PropertyNamingStrategy.UPPER_CAMEL_CASE
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
        enable(SerializationFeature.INDENT_OUTPUT)

        registerModule(SimpleModule("EscapeStrModule").apply {
            addSerializer(String::class.java, object: JsonSerializer<String>() {
                override fun serialize(value: String?, gen: JsonGenerator?, serializers: SerializerProvider?) {
                    val v = value?.replace(escapeRegex, " ")
                    return stringSerializer.serialize(v, gen, serializers)
                }
            })
        })
    }

    fun serialize(serializable: Any): String = xml.writeValueAsString(serializable)
}