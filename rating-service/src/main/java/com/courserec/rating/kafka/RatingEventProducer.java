package com.courserec.rating.kafka;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
public class RatingEventProducer {
  private static final Logger logger = LoggerFactory.getLogger(RatingEventProducer.class);
  private final KafkaTemplate<String, RatingSubmittedEvent> kafkaTemplate;
  private final String ratingsTopic;
  private final String ratingsDlqTopic;

  public RatingEventProducer(
      KafkaTemplate<String, RatingSubmittedEvent> kafkaTemplate,
      @Value("${kafka.topics.ratings}") String ratingsTopic,
      @Value("${kafka.topics.ratings-dlq}") String ratingsDlqTopic) {
    this.kafkaTemplate = kafkaTemplate;
    this.ratingsTopic = ratingsTopic;
    this.ratingsDlqTopic = ratingsDlqTopic;
  }

  public void publishRatingSubmittedEvent(RatingSubmittedEvent event) {
    String partitionKey = event.getUserId().toString();

    try {
      CompletableFuture<SendResult<String, RatingSubmittedEvent>> future =
          kafkaTemplate.send(ratingsTopic, partitionKey, event);

      future.whenComplete(
          (result, exception) -> {
            if (exception != null) {
              logger.error(
                  "Failed to publish rating event for ratingId: {}, userId: {}, courseId: {}",
                  event.getRatingId(),
                  event.getUserId(),
                  event.getCourseId(),
                  exception);
              handleFailedEvent(event, exception);
            } else {
              logger.info(
                  "Successfully published rating event - ratingId: {}, userId: {}, courseId: {}, partition: {}, offset: {}",
                  event.getRatingId(),
                  event.getUserId(),
                  event.getCourseId(),
                  result.getRecordMetadata().partition(),
                  result.getRecordMetadata().offset());
            }
          });
    } catch (Exception e) {
      logger.error("Error publishing rating event: {}", e.getMessage(), e);
      handleFailedEvent(event, e);
    }
  }

  private void handleFailedEvent(RatingSubmittedEvent event, Throwable exception) {
    try {
      String partitionKey = event.getUserId().toString();
      kafkaTemplate.send(ratingsDlqTopic, partitionKey, event);
      logger.warn("Sent failed event to DLQ - ratingId: {}", event.getRatingId());
    } catch (Exception e) {
      logger.error("Failed to send event to DLQ - ratingId: {}", event.getRatingId(), e);
    }
  }
}

