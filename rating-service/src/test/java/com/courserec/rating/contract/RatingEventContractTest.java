package com.courserec.rating.contract;

import com.courserec.rating.kafka.RatingSubmittedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RatingEventContractTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void testRatingSubmittedEventSerialization() throws Exception {
    RatingSubmittedEvent event = new RatingSubmittedEvent();
    event.setRatingId(java.util.UUID.randomUUID());
    event.setUserId(java.util.UUID.randomUUID());
    event.setCourseId(java.util.UUID.randomUUID());
    event.setRatingValue(5);

    String json = objectMapper.writeValueAsString(event);
    assertNotNull(json);
    assertTrue(json.contains("rating_id"));
    assertTrue(json.contains("user_id"));
    assertTrue(json.contains("course_id"));
    assertTrue(json.contains("rating_value"));

    RatingSubmittedEvent deserialized = objectMapper.readValue(json, RatingSubmittedEvent.class);
    assertEquals(event.getRatingId(), deserialized.getRatingId());
    assertEquals(event.getUserId(), deserialized.getUserId());
    assertEquals(event.getCourseId(), deserialized.getCourseId());
    assertEquals(event.getRatingValue(), deserialized.getRatingValue());
  }

  @Test
  void testRatingSubmittedEventRequiredFields() {
    RatingSubmittedEvent event = new RatingSubmittedEvent();
    assertThrows(Exception.class, () -> {
      objectMapper.writeValueAsString(event);
    });
  }
}

