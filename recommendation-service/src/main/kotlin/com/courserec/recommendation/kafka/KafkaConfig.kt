package com.courserec.recommendation.kafka

import io.ktor.server.application.Application
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.Properties

data class KafkaConsumerConfig(
    val bootstrapServers: String,
    val groupId: String,
    val topic: String
)

fun Application.getKafkaConfig(): KafkaConsumerConfig {
    val config = environment.config
    return KafkaConsumerConfig(
        bootstrapServers = config.property("kafka.bootstrapServers").getString(),
        groupId = config.property("kafka.consumer.groupId").getString(),
        topic = config.property("kafka.topics.ratings").getString()
    )
}

fun createKafkaConsumerProperties(kafkaConfig: KafkaConsumerConfig): Properties {
    val props = Properties()
    props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.bootstrapServers
    props[ConsumerConfig.GROUP_ID_CONFIG] = kafkaConfig.groupId
    props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
    props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
    props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
    props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
    return props
}

