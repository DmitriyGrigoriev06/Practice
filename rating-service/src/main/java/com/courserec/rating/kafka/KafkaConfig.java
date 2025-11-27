package com.courserec.rating.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
  @Bean
  public NewTopic ratingsTopic() {
    return TopicBuilder.name("ratings").partitions(3).replicas(1).build();
  }

  @Bean
  public NewTopic ratingsDlqTopic() {
    return TopicBuilder.name("ratings-dlq").partitions(1).replicas(1).build();
  }
}

