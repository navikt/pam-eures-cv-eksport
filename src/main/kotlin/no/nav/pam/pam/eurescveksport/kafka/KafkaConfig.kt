package no.nav.pam.pam.eurescveksport.kafka

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

@Configuration
class KafkaConfig {

    @Bean
    fun kafkaListenerContainerFactory(kafkaProperties: KafkaProperties): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory: ConcurrentKafkaListenerContainerFactory<String, String> = ConcurrentKafkaListenerContainerFactory()
        factory.consumerFactory = DefaultKafkaConsumerFactory(kafkaProperties.buildConsumerProperties())
        factory.setConcurrency(1)
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        return factory
    }


}
