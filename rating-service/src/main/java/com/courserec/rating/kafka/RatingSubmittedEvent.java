package com.courserec.rating.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public class RatingSubmittedEvent {
  @JsonProperty("rating_id")
  private UUID ratingId;

  @JsonProperty("user_id")
  private UUID userId;

  @JsonProperty("course_id")
  private UUID courseId;

  @JsonProperty("rating_value")
  private Integer ratingValue;

  @JsonProperty("submitted_at")
  private Instant submittedAt;

  public RatingSubmittedEvent() {}

  public RatingSubmittedEvent(UUID ratingId, UUID userId, UUID courseId, Integer ratingValue) {
    this.ratingId = ratingId;
    this.userId = userId;
    this.courseId = courseId;
    this.ratingValue = ratingValue;
    this.submittedAt = Instant.now();
  }

  public UUID getRatingId() {
    return ratingId;
  }

  public void setRatingId(UUID ratingId) {
    this.ratingId = ratingId;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public UUID getCourseId() {
    return courseId;
  }

  public void setCourseId(UUID courseId) {
    this.courseId = courseId;
  }

  public Integer getRatingValue() {
    return ratingValue;
  }

  public void setRatingValue(Integer ratingValue) {
    this.ratingValue = ratingValue;
  }

  public Instant getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(Instant submittedAt) {
    this.submittedAt = submittedAt;
  }
}

