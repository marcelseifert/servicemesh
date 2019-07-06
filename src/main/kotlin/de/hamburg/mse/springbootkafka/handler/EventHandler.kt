package de.hamburg.mse.springbootkafka.handler

import de.hamburg.mse.springbootkafka.KafkaConfiguration.Companion.TOPIC
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class EventHandler {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(EventHandler::class.java)
    }

    /*@KafkaListener(topics = [TOPIC])
    fun consume(@Payload person: Person, ack: Acknowledgment) {

        LOGGER.info("it works " + person);
        ack.acknowledge();
    }

     */

    @KafkaListener(topics = [TOPIC], groupId = "Test")
    fun receive(payload: String) {
        LOGGER.info("Received payload='$payload'")
    }

}