package com.razal.rbtticketbooking.booking.kafka.config

import com.razal.rbtticketbooking.booking.dto.FinalizeRequestKafka
import com.razal.rbtticketbooking.booking.dto.ReserveRequestKafka
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig(
    private val kafkaProperties: KafkaProperties
) {
    @Bean
    fun finalizeProducerFactory(): ProducerFactory<String, FinalizeRequestKafka> {
        val props = HashMap(kafkaProperties.buildProducerProperties(null))
        props[org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    fun finalizeKafkaTemplate(): KafkaTemplate<String, FinalizeRequestKafka> =
        KafkaTemplate(finalizeProducerFactory())

    @Bean
    fun reserveConsumerFactory(): ConsumerFactory<String, ReserveRequestKafka> {
        val props = HashMap(kafkaProperties.buildConsumerProperties(null))
        val valueDeserializer = JsonDeserializer(ReserveRequestKafka::class.java).apply {
            addTrustedPackages("com.razal.rbtticketbooking.*")
        }
        return DefaultKafkaConsumerFactory(
            props,
            StringDeserializer(),
            valueDeserializer
        )
    }
    @Bean
    fun reserveKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, ReserveRequestKafka> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, ReserveRequestKafka>()
        factory.consumerFactory = reserveConsumerFactory()
        return factory
    }

    @Bean
    fun finalizeConsumerFactory(): ConsumerFactory<String, FinalizeRequestKafka> {
        val props = HashMap(kafkaProperties.buildConsumerProperties(null))
        val valueDeserializer = JsonDeserializer(FinalizeRequestKafka::class.java).apply {
            addTrustedPackages("com.razal.rbtticketbooking.*")
        }
        return DefaultKafkaConsumerFactory(
            props,
            StringDeserializer(),
            valueDeserializer
        )
    }
    @Bean
    fun finalizeKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, FinalizeRequestKafka> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, FinalizeRequestKafka>()
        factory.consumerFactory = finalizeConsumerFactory()
        return factory
    }
}