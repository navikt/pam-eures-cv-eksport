package no.nav.pam.pam.eurescveksport.kafka

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.retry.RetryPolicy
import org.springframework.retry.backoff.BackOffPolicy
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate
import java.io.IOException
import java.util.*

@Configuration
class KafkaConfig {

    @Bean
    fun kafkaListenerContainerFactory(kafkaProperties: KafkaProperties, @Value("\${kafka.consumer.concurrency}") kafkaConcurrency: Int)
            : ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory: ConcurrentKafkaListenerContainerFactory<String, String> = ConcurrentKafkaListenerContainerFactory()
        factory.consumerFactory = DefaultKafkaConsumerFactory(kafkaProperties.buildConsumerProperties())
        factory.setConcurrency(kafkaConcurrency)
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        return factory
    }

    @Autowired
    @Bean
    fun retryTemplate(@Value("\${kafka.retry.max}") maxRetries: Int,
                      @Value("\${kafka.retry.initial_interval}") initialInterval: Int,
                      @Value("\${kafka.retry.multiplier}") multiplier: Double,
                      @Value("\${kafka.retry.max_interval}") maxInterval: Int): RetryTemplate? {
        val retryTemplate = RetryTemplate()
        retryTemplate.setRetryPolicy(retryPolicy(maxRetries))
        retryTemplate.setBackOffPolicy(backoffPolicy(initialInterval, multiplier, maxInterval))
        retryTemplate.setThrowLastExceptionOnExhausted(true)
        return retryTemplate
    }

    private fun retryPolicy(maxRetries: Int): RetryPolicy {
        val retriableExceptions: MutableMap<Class<out Throwable?>, Boolean> = HashMap()
        retriableExceptions[IOException::class.java] = true
        return SimpleRetryPolicy(maxRetries, retriableExceptions)
    }

    private fun backoffPolicy(initialInterval: Int, multiplier: Double, maxInterval: Int): BackOffPolicy {
        val policy = ExponentialBackOffPolicy()
        policy.initialInterval = initialInterval.toLong()
        policy.multiplier = multiplier
        policy.maxInterval = maxInterval.toLong()
        return policy
    }

}
