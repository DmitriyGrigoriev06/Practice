package com.courserec.recommendation.kafka

import com.courserec.recommendation.service.RecommendationService
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.UUID
import java.util.Properties

class RatingEventConsumer(
    private val kafkaConfig: com.courserec.recommendation.kafka.KafkaConsumerConfig,
    private val recommendationService: RecommendationService
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val logger = LoggerFactory.getLogger(RatingEventConsumer::class.java)
    private var consumer: KafkaConsumer<String, String>? = null
    private var running = false

    fun start() {
        val props = com.courserec.recommendation.kafka.createKafkaConsumerProperties(kafkaConfig)
        consumer = KafkaConsumer<String, String>(props)
        consumer?.subscribe(listOf(kafkaConfig.topic))
        running = true

        Thread {
            while (running) {
                try {
                    val records: ConsumerRecords<String, String> = consumer?.poll(Duration.ofMillis(100)) ?: break
                    for (record in records) {
                        processRecord(record)
                    }
                } catch (e: Exception) {
                    logger.error("Error consuming Kafka messages: {}", e.message, e)
                }
            }
        }.start()

        logger.info("Kafka consumer started for topic: {}", kafkaConfig.topic)
    }

    fun stop() {
        running = false
        consumer?.close()
        logger.info("Kafka consumer stopped")
    }

    private fun processRecord(record: ConsumerRecord<String, String>) {
        try {
            val event = json.decodeFromString<RatingSubmittedEvent>(record.value())
            logger.info(
                "Received rating event - ratingId: {}, userId: {}, courseId: {}, ratingValue: {}, partition: {}, offset: {}",
                event.ratingId,
                event.userId,
                event.courseId,
                event.ratingValue,
                record.partition(),
                record.offset()
            )

            // Invalidate cache for the user to trigger recalculation
            recommendationService.invalidateCache(UUID.fromString(event.userId))

            // Note: Actual recalculation will happen on next request
            // For immediate recalculation, uncomment the following:
            // recommendationService.generateRecommendations(UUID.fromString(event.userId))
        } catch (e: Exception) {
            logger.error("Error processing rating event: {}", e.message, e)
        }
    }
}

@kotlinx.serialization.Serializable
data class RatingSubmittedEvent(
    val ratingId: String,
    val userId: String,
    val courseId: String,
    val ratingValue: Int,
    val submittedAt: String
)

