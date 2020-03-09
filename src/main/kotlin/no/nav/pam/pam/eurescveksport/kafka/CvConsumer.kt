package no.nav.pam.pam.eurescveksport.kafka

import no.nav.arbeid.cv.avro.Melding
import no.nav.arbeid.cv.avro.Meldingstype
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component


@Component
class CvConsumer(private val retryTemplate: RetryTemplate) {
    private val logger = LoggerFactory.getLogger(javaClass)
    @KafkaListener(topics = ["\${kafka.cvtopic}"])
    fun onMessage(record: ConsumerRecord<String, Melding>, ack: Acknowledgment) {
        logger.info("got message: {}", record)
        retryTemplate.execute<Any?, RuntimeException> {
            processMessage(record.value())
        }
        ack.acknowledge()
    }

    private fun processMessage(melding: Melding) {

        when (melding.getMeldingstype()) {
            Meldingstype.SLETT -> print("SLETT")
            Meldingstype.ENDRE -> print("ENDRE")
            Meldingstype.OPPRETT -> print("OPPRETT")
            else -> {
                print("Ukjent meldingstype")
            }
        }


    }
}
