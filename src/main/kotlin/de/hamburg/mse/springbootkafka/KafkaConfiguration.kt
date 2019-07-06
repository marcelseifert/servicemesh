package de.hamburg.mse.springbootkafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import de.hamburg.mse.springbootkafka.model.Person
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.kafka.listener.SeekToCurrentErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.retry.backoff.FixedBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate

@EnableKafka
@Configuration
class KafkaConfiguration(@Autowired
                         val kafkaProperties: KafkaProperties) {

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Person> {
        var mapper = ObjectMapper()
                .registerModule(JavaTimeModule())
                .registerModule(ParameterNamesModule())
                .registerModule(Jdk8Module())
                .registerModule(KotlinModule())
        val jsonDeserializer = JsonDeserializer(Person::class.java, mapper)
        jsonDeserializer.addTrustedPackages("*")
        kafkaProperties.consumer.autoOffsetReset = "earliest"
        //kafkaProperties.consumer.enableAutoCommit = false
        return DefaultKafkaConsumerFactory(
                kafkaProperties.buildConsumerProperties(),
                StringDeserializer(),
                jsonDeserializer)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Person> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Person>()
        factory.consumerFactory = consumerFactory()
        /*val seekToCurrentErrorHandler = SeekToCurrentErrorHandler( { consumerRecord, exception ->
            LOG.info("well $consumerRecord")
        },10) */
        val seekToCurrentErrorHandler = MySeekHandler()
        seekToCurrentErrorHandler.setCommitRecovered(true)
        factory.setErrorHandler(seekToCurrentErrorHandler)
        factory.setRetryTemplate(retryTemplate())
        //factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.setRecoveryCallback { _ ->
            LOG.error("RetryPolicy limit has been exceeded! You should really handle this better.")
        }
        return factory
    }

    @Bean
    fun retryTemplate(): RetryTemplate {
        val retryTemplate = RetryTemplate()

        val fixedBackOffPolicy = FixedBackOffPolicy()
        fixedBackOffPolicy.backOffPeriod = 10000L
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy)

        val retryPolicy = SimpleRetryPolicy()
        retryPolicy.maxAttempts = 10
        retryTemplate.setRetryPolicy(retryPolicy)

        return retryTemplate
    }


    companion object {
        const val TOPIC = "Person"
        private val LOG = LoggerFactory.getLogger(KafkaConfiguration::class.java)
    }
}

class MySeekHandler : SeekToCurrentErrorHandler() {
    companion object {
        private val LOG = LoggerFactory.getLogger(MySeekHandler::class.java)
    }

    override fun handle(thrownException: Exception?, records: MutableList<ConsumerRecord<*, *>>?, consumer: Consumer<*, *>?, container: MessageListenerContainer?) =
            if (thrownException is SerializationException) {
                LOG.warn(thrownException.cause!!.message)
                LOG.warn(thrownException.message)
                val message = thrownException.message!!.split("Error deserializing key/value for partition ")[1].split(". If needed, please seek past the record to continue consumption.")[0]
                val topics = message.split("-")[0]
                val offset = Integer.valueOf(message.split("offset ")[1])
                val partition = Integer.valueOf(message.split("-")[1].split(" at")[0])

                val topicPartition = TopicPartition(topics, partition)
                //log.info("Skipping " + topic + "-" + partition + " offset " + offset);
                consumer?.seek(topicPartition, (offset + 1).toLong())
                LOG.warn("seek forward to  $topicPartition to ${offset + 1}")
            } else {
                super.handle(thrownException, records, consumer, container)
            }
}