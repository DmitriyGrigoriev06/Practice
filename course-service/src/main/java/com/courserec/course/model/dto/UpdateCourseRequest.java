package com.courserec.course.model.dto;

import jakarta.validation.constraints.Size;

public class UpdateCourseRequest {
  @Size(max = 255, message = "Title must not exceed 255 characters")
  private String title;

  @Size(max = 10000, message = "Description must not exceed 10000 characters")
  private String description;

  private String category;

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
}

