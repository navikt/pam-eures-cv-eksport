package no.nav.cv.eures.cv

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class CvAvroSchema (private val schemaClient: CvAvroSchemaClient) {

    companion object {
        val log = LoggerFactory.getLogger(CvAvroSchema::class.java)
    }

    fun getSchema(version: Int) = schemaClient.getSchema(version)
}

@Client("\${kafka.schema.registry.url}")
interface CvAvroSchemaClient {

    @Get("/subjects/\${avro.schema.subject}/versions/{version}/schema")
    fun getSchema(version: Int): String

}