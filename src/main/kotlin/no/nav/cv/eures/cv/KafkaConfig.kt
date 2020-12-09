package no.nav.cv.eures.cv

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.SeekToCurrentBatchErrorHandler
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.*

@Configuration
@EnableKafka
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    lateinit var groupId: String

    companion object {
        private val log = LoggerFactory.getLogger(KafkaConfig::class.java)
    }

    @Bean(name = ["cvMeldingContainerFactory"])
    fun meldingContainerFactory(consumerFactory: ConsumerFactory<String, ByteArray>): ConcurrentKafkaListenerContainerFactory<String, ByteArray>? {
        val mergedProps = defaultConsumerConfigs().apply {
            putAll(consumerFactory.configurationProperties)
        }

        return ConcurrentKafkaListenerContainerFactory<String, ByteArray>().apply {
            setConcurrency(1)
            setConsumerFactory(meldingConsumerFactory(mergedProps))
            containerProperties.pollTimeout = Long.MAX_VALUE
            containerProperties.consumerTaskExecutor = containerExecutor()
            isBatchListener = true
            //setRetryTemplate(retryTemplate())
            setBatchErrorHandler(KafkaErrorHandler())
        }
    }

    @Bean
    fun containerExecutor(): ThreadPoolTaskExecutor = ThreadPoolTaskExecutor().apply { corePoolSize = 10 }

    fun defaultConsumerConfigs(
    ): MutableMap<String, Any> {
        val map: MutableMap<String, Any> = hashMapOf()
        //map[AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = schemaRegistry
        map[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        map[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        map[ConsumerConfig.CLIENT_ID_CONFIG] = System.getenv("POD_NAME") ?: "pam-eures-cv-eksport-${UUID.randomUUID()}"
        return map
    }

    fun meldingConsumerFactory(configs: Map<String, Any> = defaultConsumerConfigs()): DefaultKafkaConsumerFactory<String, ByteArray>
            = DefaultKafkaConsumerFactory(configs)
}