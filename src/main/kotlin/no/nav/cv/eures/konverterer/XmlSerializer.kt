package no.nav.cv.eures.konverterer

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.ValueSerializer
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.dataformat.xml.XmlMapper
import tools.jackson.dataformat.xml.XmlWriteFeature
import tools.jackson.module.kotlin.KotlinModule

object XmlSerializer {
    private val escapeRegex =
            Regex("[^\u0009\u000A\u000D\u0020-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]")
    private val xml: XmlMapper = XmlMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .propertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
        .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
        .enable(XmlWriteFeature.WRITE_XML_DECLARATION)
        .enable(SerializationFeature.INDENT_OUTPUT)
        .addModule(SimpleModule("EscapeStrModule").apply {
            addSerializer(String::class.java, object: ValueSerializer<String>() {
                override fun serialize(value: String, gen: JsonGenerator, serializers: SerializationContext) {
                    gen.writeString(value.replace(escapeRegex, " "))
                }
            })
        })
        .build()

    fun serialize(serializable: Any): String = xml.writeValueAsString(serializable)
}