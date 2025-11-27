package com.courserec.course.model.dto;

import com.courserec.course.model.Course;
import java.time.Instant;
import java.util.UUID;

public class CourseResponse {
  private UUID id;
  private String title;
  private String description;
  private String category;
  private Instant createdAt;
  private Instant updatedAt;
  private UUID createdBy;

  public CourseResponse() {}

  public CourseResponse(Course course) {
    this.id = course.getId();
    this.title = course.getTitle();
    this.description = course.getDescription();
    this.category = course.getCategory();
    this.createdAt = course.getCreatedAt();
    this.updatedAt = course.getUpdatedAt();
    this.createdBy = course.getCreatedBy();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
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

  public UUID getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(UUID createdBy) {
    this.createdBy = createdBy;
  }
}

