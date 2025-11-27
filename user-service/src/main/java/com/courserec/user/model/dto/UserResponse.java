package com.courserec.user.model.dto;

import com.courserec.user.model.User;
import java.time.Instant;
import java.util.UUID;

public class UserResponse {
  private UUID id;
  private String email;
  private String role;
  private String accountStatus;
  private Instant createdAt;
  private Instant updatedAt;

  public UserResponse() {}

  public UserResponse(User user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.role = user.getRole().name();
    this.accountStatus = user.getAccountStatus().name();
    this.createdAt = user.getCreatedAt();
    this.updatedAt = user.getUpdatedAt();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getAccountStatus() {
    return accountStatus;
  }

  public void setAccountStatus(String accountStatus) {
    this.accountStatus = accountStatus;
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

