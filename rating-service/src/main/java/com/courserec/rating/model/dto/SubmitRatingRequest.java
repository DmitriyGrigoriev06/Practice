package com.courserec.rating.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class SubmitRatingRequest {
  @NotNull(message = "Course ID is required")
  private UUID courseId;

  @NotNull(message = "Rating value is required")
  @Min(value = 1, message = "Rating value must be at least 1")
  @Max(value = 5, message = "Rating value must be at most 5")
  private Integer ratingValue;

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
}

