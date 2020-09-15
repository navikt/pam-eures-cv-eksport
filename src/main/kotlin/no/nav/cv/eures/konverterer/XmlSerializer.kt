package no.nav.cv.eures.konverterer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.cv.eures.model.Candidate
import java.io.File

object XmlSerializer {

    private val xml: XmlMapper = XmlMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        propertyNamingStrategy = PropertyNamingStrategy.UPPER_CAMEL_CASE
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    fun serialize(candidate:Candidate): String {
        val xmlString = xml.writeValueAsString(candidate)

        val filename = "cv_${candidate.documentId.uuid}.xml"

        File(filename).writeText(xmlString)

        return xmlString
    }

}