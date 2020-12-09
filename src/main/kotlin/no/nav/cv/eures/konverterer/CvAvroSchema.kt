package no.nav.cv.eures.konverterer

import org.apache.avro.Schema
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap


@Service
class CvAvroSchema(private val schemaClient: CvAvroSchemaClient) {

    companion object {
        val log = LoggerFactory.getLogger(CvAvroSchema::class.java)
    }

    val schemas = ConcurrentHashMap<Int, Schema>()

    fun getSchema(wireBytes: ByteArray): Schema {

        // https://docs.confluent.io/current/schema-registry/serdes-develop/index.html#messages-wire-format
        val schemaVersioBuffer = ByteBuffer.wrap(wireBytes.slice(1..4).toByteArray())
        val schemaVersion = schemaVersioBuffer.getInt(0)
        log.debug("Schema version $schemaVersion")

        return schemas.getOrPut(schemaVersion) {
            val jsonSchema = schemaClient.getSchema(schemaVersion)

            log.debug("SCHEMA version $schemaVersion: $jsonSchema")

            return Schema.Parser().parse(jsonSchema)
        }

    }
}

@Service
class CvAvroSchemaClient(
        @Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String,
        @Value("\${avro.schema.subject}") private val schemaSubject: String,
        @Autowired private val client: RestTemplate
) {

    fun getSchema(version: Int): String {
        val uri  = "${schemaRegistryUrl}/subjects/${schemaSubject}/versions/${version}/schema"
        return client.getForObject(uri, String::class.java)!!

    }
}
