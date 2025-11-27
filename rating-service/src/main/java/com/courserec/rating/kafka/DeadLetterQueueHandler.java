package com.courserec.rating.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterQueueHandler {
  private static final Logger logger = LoggerFactory.getLogger(DeadLetterQueueHandler.class);

  @KafkaListener(topics = "${kafka.topics.ratings-dlq}", groupId = "rating-service-dlq-group")
  public void handleDeadLetterMessage(RatingSubmittedEvent event) {
    logger.error(
        "Received event in DLQ - ratingId: {}, userId: {}, courseId: {}, ratingValue: {}, submittedAt: {}",
        event.getRatingId(),
        event.getUserId(),
        event.getCourseId(),
        event.getRatingValue(),
        event.getSubmittedAt());
    // TODO: Implement retry logic or alerting mechanism
  }
}

