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
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.Duration
import java.util.*

@Configuration
@EnableKafka
class KafkaConfig {


    @Value("\${kafka.aiven.keystorePath}")
    lateinit var keyStorePath: String

    @Value("\${kafka.aiven.credstorePassword}")
    lateinit var credstorePassword: String

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
            //gammel setBatchErrorHandler(KafkaErrorHandler())
            //ny setCommonErrorHandler(CommonErrorHandler())
        }
    }

    @Bean
    fun consumerFactoryInternCvTopic() : ConsumerFactory<String, String> {
        val props: MutableMap<String, Any> = hashMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to brokers,
            ConsumerConfig.GROUP_ID_CONFIG to groupidInternTopic,
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
}