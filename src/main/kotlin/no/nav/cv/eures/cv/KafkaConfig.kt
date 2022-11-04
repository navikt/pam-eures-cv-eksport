package no.nav.cv.eures.cv

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.Duration
import java.util.*

@Configuration
@EnableKafka
class KafkaConfig {

    @Value("\${kafka.onprem.bootstrap-servers}")
    lateinit var bootstrapServers: String

    @Value("\${kafka.onprem.consumer.group-id}")
    lateinit var groupId: String

    @Value("\${kafka.aiven.keystorePath}")
    lateinit var keyStorePath: String

    @Value("\${kafka.aiven.credstorePassword}")
    lateinit var credstorePassword: String

    @Value("\${kafka.aiven.truststorePath}")
    lateinit var truststorePath: String

    @Value("\${kafka.aiven.brokers}")
    lateinit var brokers: String

    @Value("\${kafka.onprem.ssl.trust-store-location}")
    lateinit var trustStoreLocationOnPrem: String

    @Value("\${kafka.onprem.ssl.trust-store-password}")
    lateinit var trustStorePasswordOnPrem: String

    @Value("\${kafka.onprem.schema.registry.url}")
    lateinit var schemaRegistryOnPrem: String

    @Value("\${kafka.onprem.security.protocol}")
    lateinit var onpremSecurityProtocol: String



    companion object {
        private val log = LoggerFactory.getLogger(KafkaConfig::class.java)
    }

    @Bean(name = ["cvMeldingContainerFactory"])
    fun onpremKafkaListenerConstainerFactory() : ConcurrentKafkaListenerContainerFactory<String, ByteArray> {
        return ConcurrentKafkaListenerContainerFactory<String, ByteArray>().apply{
            setConcurrency(1)
            setConsumerFactory(consumerFactoryOnPrem())
            containerProperties.pollTimeout = Long.MAX_VALUE
            containerProperties.consumerTaskExecutor = containerExecutor()
            setBatchListener(true)
            containerProperties.authorizationExceptionRetryInterval = Duration.ofSeconds(60)
            setBatchErrorHandler(KafkaErrorHandler())
        }
    }

    @Bean
    fun containerExecutor(): ThreadPoolTaskExecutor = ThreadPoolTaskExecutor().apply { corePoolSize = 10 }

    @Bean(name = ["internCvTopicContainerFactory"])
    fun internCvTopicKafkaListenerConstainerFactory() : ConcurrentKafkaListenerContainerFactory<String, String> {
        return ConcurrentKafkaListenerContainerFactory<String, String>().apply{
            setConcurrency(1)
            setConsumerFactory(consumerFactoryInternCvTopic())
            containerProperties.pollTimeout = Long.MAX_VALUE
            containerProperties.consumerTaskExecutor = containerExecutor()
            setBatchListener(true)
            containerProperties.authorizationExceptionRetryInterval = Duration.ofSeconds(60)
            setBatchErrorHandler(KafkaErrorHandler())
        }
    }

    @Bean
    fun consumerFactoryInternCvTopic() : ConsumerFactory<String, String> {
        val props: MutableMap<String, Any> = hashMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to brokers,
            ConsumerConfig.GROUP_ID_CONFIG to "pam-eures-cv-eksport-v111",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.CLIENT_ID_CONFIG to (System.getenv("POD_NAME") ?: "pam-eures-cv-eksport-${UUID.randomUUID()}"),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to true,

            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
            SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to "",
            SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
            SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
            SslConfigs.SSL_KEY_PASSWORD_CONFIG to credstorePassword
        )
        System.getenv("KAFKA_KEYSTORE_PATH")?.let {
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStorePath)
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, credstorePassword)
        }
        System.getenv("KAFKA_TRUSTSTORE_PATH")?.let {
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath)
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, credstorePassword)
        }
        return DefaultKafkaConsumerFactory<String, String>(props)
    }

    @Bean
    fun consumerFactoryOnPrem() : ConsumerFactory<String, ByteArray> {
        val props: MutableMap<String, Any> = hashMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.CLIENT_ID_CONFIG to (System.getenv("POD_NAME") ?: "pam-eures-cv-eksport-${UUID.randomUUID()}"),
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.canonicalName,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java.canonicalName,

            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to onpremSecurityProtocol,
            SaslConfigs.SASL_MECHANISM to "PLAIN",
            SaslConfigs.SASL_JAAS_CONFIG to "no.nav.cv.eures.cv.NaisLoginModule required;",
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to trustStoreLocationOnPrem,
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to trustStorePasswordOnPrem,

            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to "true",
            KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryOnPrem,
            KafkaAvroDeserializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO",
            KafkaAvroDeserializerConfig.USER_INFO_CONFIG to "${System.getenv("KAFKA_SCHEMA_REGISTRY_USER")}:${System.getenv("KAFKA_SCHEMA_REGISTRY_PASSWORD")}",

            )
        return DefaultKafkaConsumerFactory<String, ByteArray>(props)
    }
}