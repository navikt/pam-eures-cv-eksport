package no.nav.cv.eures.konverterer

import org.apache.avro.Schema
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.nio.ByteBuffer
import javax.security.auth.Subject

@Service
class CvAvroSchema(private val schemaClient: CvAvroSchemaClient) {

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

@Service
class CvAvroSchemaClient(
        @Value("\${kafka.schema.registry.url}") private val schemaRegistryUrl: String,
        @Value("\${avro.schema.subject}") private val schemaSubject: String
) {

    private var client: WebClient = WebClient
            .builder()
            .baseUrl(schemaRegistryUrl)
            .build()

    fun getSchema(version: Int): String {
        return client.get()
                .uri("/subjects/${schemaSubject}/versions/${version}/schema")
                .retrieve()
                .bodyToMono(String::class.java)
                .block()!!

    }
}
