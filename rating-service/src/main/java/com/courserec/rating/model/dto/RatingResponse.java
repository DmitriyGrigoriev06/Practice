package com.courserec.rating.model.dto;

import com.courserec.rating.model.Rating;
import java.time.Instant;
import java.util.UUID;

public class RatingResponse {
  private UUID id;
  private UUID userId;
  private UUID courseId;
  private Integer ratingValue;
  private Instant createdAt;
  private Instant updatedAt;

  public RatingResponse() {}

  public RatingResponse(Rating rating) {
    this.id = rating.getId();
    this.userId = rating.getUserId();
    this.courseId = rating.getCourseId();
    this.ratingValue = rating.getRatingValue();
    this.createdAt = rating.getCreatedAt();
    this.updatedAt = rating.getUpdatedAt();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}

