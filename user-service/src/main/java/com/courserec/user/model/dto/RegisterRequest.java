package com.courserec.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Schema(description = "User email address", example = "user@example.com")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  @Schema(description = "User password (minimum 8 characters)", example = "password123")
  private String password;

  @Schema(
      description = "User role",
      example = "USER",
      allowableValues = {"USER", "ADMIN"},
      defaultValue = "USER")
  private String role;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}

