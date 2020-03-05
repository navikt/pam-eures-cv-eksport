package no.nav.pam.pam.eurescveksport.kafka1

import no.nav.arbeid.cv.avro.Melding
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class CvConsumer {
    private val logger = LoggerFactory.getLogger(javaClass)
    @KafkaListener(topics = ["\${kafka.cvtopic}"])
    fun processMessage(record: ConsumerRecord<String, Melding>, ack: Acknowledgment) {
        logger.info("got message: {}", record)
        ack.acknowledge()
    }
}
