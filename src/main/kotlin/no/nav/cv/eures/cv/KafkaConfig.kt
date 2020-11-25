//package no.nav.cv.eures.cv
//
//import org.apache.kafka.common.serialization.ByteArrayDeserializer
//import org.apache.kafka.common.serialization.StringDeserializer
//import org.slf4j.LoggerFactory
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.kafka.annotation.EnableKafka
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
//import org.springframework.kafka.config.KafkaListenerContainerFactory
//import org.springframework.kafka.core.ConsumerFactory
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory
//import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
//
//@Configuration
//@EnableKafka
//class KafkaConfig {
//
//
//    companion object {
//        private val log = LoggerFactory.getLogger(KafkaConfig::class.java)
//    }
//
//    @Bean("kafkaListenerContainerFactory")
//    fun kafkaListenerContainerFactory(): KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, ByteArray>> {
//        val factory = ConcurrentKafkaListenerContainerFactory<String, ByteArray>();
//        factory.setConsumerFactory(consumerFactory());
//        factory.setConcurrency(3);
//        factory.getContainerProperties().setPollTimeout(3000);
//        return factory;
//    }
//
//    @Bean
//    fun consumerFactory(): ConsumerFactory<String, ByteArray> {
//        return DefaultKafkaConsumerFactory(
//                consumerConfigs(),
//                StringDeserializer(),
//                ByteArrayDeserializer())
//    }
//
//    @Bean
//    fun consumerConfigs(): Map<String, Any> {
//        val props = mutableMapOf<String, Any>()
//        //props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
//        return props
//    }
//}