package no.nav.cv.eures.konverterer

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import org.apache.avro.Schema
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import javax.inject.Singleton

@Singleton
class CvAvroSchema (private val schemaClient: CvAvroSchemaClient) {

    companion object {
        val log = LoggerFactory.getLogger(CvAvroSchema::class.java)
    }

    fun getSchema(wireBytes: ByteArray): Schema {

        // https://docs.confluent.io/current/schema-registry/serdes-develop/index.html#messages-wire-format
        val schemaVersioBuffer = ByteBuffer.wrap(wireBytes.slice(1..4).toByteArray())
        val schemaVersion = schemaVersioBuffer.getInt(0)

        val jsonSchema = schemaClient.getSchema(schemaVersion)

        log.debug("SCHEMA version $schemaVersion: $jsonSchema")

        return Schema.Parser().parse(jsonSchema)
    }
}

@Client("\${kafka.schema.registry.url}")
interface CvAvroSchemaClient {

    @Get("/subjects/\${avro.schema.subject}/versions/{version}/schema")
    fun getSchema(version: Int): String
}