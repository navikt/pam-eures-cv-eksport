package no.nav.cv.eures.cv

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.CommonContainerStoppingErrorHandler
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.Duration
import java.util.*

@Configuration
@EnableKafka
class KafkaConfig {


    @Value("\${kafka.aiven.keystorePath}")
    lateinit var keyStorePath: String

    @Value("\${kafka.aiven.credstorePassword}")
     private val credstorePassword: String? = null

    @Value("\${kafka.aiven.truststorePath}")
    lateinit var truststorePath: String

    @Value("\${kafka.aiven.brokers}")
    lateinit var brokers: String

    @Value("\${kafka.aiven.groupid}")
    lateinit var groupidInternTopic: String

    @Bean
    fun containerExecutor(): ThreadPoolTaskExecutor = ThreadPoolTaskExecutor().apply { corePoolSize = 10 }

    @Bean(name = ["internCvTopicContainerFactory"])
    fun internCvTopicKafkaListenerConstainerFactory() : ConcurrentKafkaListenerContainerFactory<String, String> {
        return ConcurrentKafkaListenerContainerFactory<String, String>().apply{
            setConcurrency(1)
            setConsumerFactory(consumerFactoryInternCvTopic())
            containerProperties.pollTimeout = Long.MAX_VALUE
            containerProperties.listenerTaskExecutor = containerExecutor()
            isBatchListener = true
            containerProperties.authExceptionRetryInterval = Duration.ofSeconds(60)
            setCommonErrorHandler(CommonContainerStoppingErrorHandler())
        }
    }

    @Bean
    fun consumerFactoryInternCvTopic() : ConsumerFactory<String, String> {
        val props: MutableMap<String, Any> = mutableMapOf<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = brokers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupidInternTopic
        props[ConsumerConfig.CLIENT_ID_CONFIG] = (System.getenv("POD_NAME") ?: "pam-eures-cv-eksport-${UUID.randomUUID()}")

        if(!credstorePassword.isNullOrBlank()) {
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = credstorePassword
            props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = credstorePassword
            props[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = credstorePassword
            props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "JKS"
            props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
            props[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = ""
        }

        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = true
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        System.getenv("KAFKA_KEYSTORE_PATH")?.let {
            props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = keyStorePath
        }
        System.getenv("KAFKA_TRUSTSTORE_PATH")?.let {
            props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = truststorePath
            props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL"
        }
        return DefaultKafkaConsumerFactory<String, String>(props)
    }
}
