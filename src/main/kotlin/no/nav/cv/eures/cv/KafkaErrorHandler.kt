package no.nav.cv.eures.cv

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory
import org.springframework.kafka.listener.SeekToCurrentBatchErrorHandler
import java.lang.Exception

class KafkaErrorHandler : SeekToCurrentBatchErrorHandler() {
    companion object {
        private val log = LoggerFactory.getLogger(KafkaErrorHandler::class.java)
    }

    override fun handle(thrownException: Exception?, data: ConsumerRecords<*, *>?) {
        val errorOffsets = data?.map { it.offset() }

        log.info("Kafka listener feilet: $errorOffsets.", thrownException)

        super.handle(thrownException, data)
    }

}