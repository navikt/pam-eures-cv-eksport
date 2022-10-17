package no.nav.cv.eures.cv

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ByteArrayResource
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.SeekToCurrentBatchErrorHandler
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.io.File
import java.time.Duration
import java.util.*

@Configuration
@EnableKafka
class KafkaConfig {

    @Value("\${kafka.onprem.bootstrap-servers}")
    lateinit var bootstrapServers: String

    @Value("\${kafka.onprem.consumer.group-id}")
    lateinit var groupId: String

    @Value("\${kafka.aiven.topics.keystorePath}")
    lateinit var keyStorePath: String

    @Value("\${kafka.aiven.topics.credstorePassword}")
    lateinit var credstorePassword: String

    @Value("\${kafka.aiven.topics.truststorePath}")
    lateinit var truststorePath: String

    @Value("\${featureToggle.internTopicOn}")
    private val internTopicOn: Boolean = true

    @Value("\${kafka.aiven.topics.brokers}")
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
            setConsumerFactory(consumerFactory())
        }
    }

    @Bean
    fun consumerFactory() : ConsumerFactory<String, ByteArray> {
        val props: MutableMap<String, Any> = hashMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.CLIENT_ID_CONFIG to (System.getenv("POD_NAME") ?: "pam-eures-cv-eksport-${UUID.randomUUID()}"),
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.canonicalName,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java.canonicalName,

            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to onpremSecurityProtocol,
            SaslConfigs.SASL_MECHANISM to "PLAIN",
            SaslConfigs.SASL_JAAS_CONFIG to "no.nav.cv.eures.cv.NaisLoginModule required;",
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to trustStoreLocationOnPrem,
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to trustStorePasswordOnPrem,

            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to "true",
            KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryOnPrem,
            KafkaAvroDeserializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO",
            KafkaAvroDeserializerConfig.USER_INFO_CONFIG to "user:pass"

            )
        return DefaultKafkaConsumerFactory<String, ByteArray>(props)
    }

    /*
    @Bean(name = ["cvMeldingContainerFactory"])
    fun meldingContainerFactory(consumerFactory: ConsumerFactory<String, Any>): ConcurrentKafkaListenerContainerFactory<String, ByteArray>? =
        ConcurrentKafkaListenerContainerFactory<String, ByteArray>().apply {
            setConcurrency(1)
            if (internTopicOn) {
                var mergedProps = consumerFactory.configurationProperties.toMutableMap().apply {
                    putAll(defaultInternTopicConsumerConfigs())
                }
                setConsumerFactory(meldingConsumerFactoryJsonTopic(mergedProps))
            } else {
                var mergedProps = defaultAvroTopicConsumerConfigs().apply {
                    putAll(consumerFactory.configurationProperties)
                }
                setConsumerFactory(meldingConsumerFactoryAvroTopic(mergedProps))
            }
            containerProperties.pollTimeout = Long.MAX_VALUE
            containerProperties.consumerTaskExecutor = containerExecutor()
            isBatchListener = true
            //setRetryTemplate(retryTemplate())
            setBatchErrorHandler(KafkaErrorHandler())
        }

    @Bean(name = ["cvEndretInternContainerFactory"])
    fun cvEndretInternContainerFactory(consumerFactory: ConsumerFactory<String, String>) =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            setConcurrency(1) //how many KafkaMessageListenerContainers are made?
            setConsumerFactory(consumerFactory)
            containerProperties.pollTimeout = Long.MAX_VALUE
            containerProperties.consumerTaskExecutor = containerExecutor() //how many ListenerConsumers per KafkaMessageListenerContainer?
            isBatchListener = true
            setBatchErrorHandler(KafkaErrorHandler())
    }



    @Bean
    fun containerExecutor(): ThreadPoolTaskExecutor = ThreadPoolTaskExecutor().apply { corePoolSize = 10 }

    fun defaultAvroTopicConsumerConfigs(
    ): MutableMap<String, Any> {
        val map: MutableMap<String, Any> = hashMapOf()
        //map[AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = schemaRegistry
        map[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        map[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        map[ConsumerConfig.CLIENT_ID_CONFIG] = System.getenv("POD_NAME") ?: "pam-eures-cv-eksport-${UUID.randomUUID()}"
        return map
    }

    fun defaultInternTopicConsumerConfigs(
    ): MutableMap<String, Any> {
        val map: MutableMap<String, Any> = hashMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to brokers,
            ConsumerConfig.GROUP_ID_CONFIG to "pam-eures-cv-eksport-v111",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.CLIENT_ID_CONFIG to (System.getenv("POD_NAME") ?: "pam-eures-cv-eksport-${UUID.randomUUID()}"),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to true,

            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
            SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to "", // Disable server host name verification
            SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
            SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to truststorePath,
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to credstorePassword,
            SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to keyStorePath,
            SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to credstorePassword,
            SslConfigs.SSL_KEY_PASSWORD_CONFIG to credstorePassword
        )
        return map
    }

    fun meldingConsumerFactoryAvroTopic(configs: Map<String, Any> = defaultAvroTopicConsumerConfigs()): DefaultKafkaConsumerFactory<String, ByteArray>
            = DefaultKafkaConsumerFactory(configs)

    fun meldingConsumerFactoryJsonTopic(configs: Map<String, Any> = defaultInternTopicConsumerConfigs()): DefaultKafkaConsumerFactory<String, ByteArray>
            = DefaultKafkaConsumerFactory(configs)
}

     */
}