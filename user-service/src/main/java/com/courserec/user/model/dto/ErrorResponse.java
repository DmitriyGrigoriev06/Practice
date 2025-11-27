package com.courserec.user.model.dto;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {
  private ErrorDetail error;

  public ErrorResponse(String code, String message, Map<String, Object> details) {
    this.error = new ErrorDetail(code, message, Instant.now(), details);
  }

  public ErrorDetail getError() {
    return error;
  }

  public void setError(ErrorDetail error) {
    this.error = error;
  }

  public static class ErrorDetail {
    private String code;
    private String message;
    private Instant timestamp;
    private Map<String, Object> details;

    public ErrorDetail(String code, String message, Instant timestamp, Map<String, Object> details) {
      this.code = code;
      this.message = message;
      this.timestamp = timestamp;
      this.details = details;
    }

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public Instant getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
      this.timestamp = timestamp;
    }

    public Map<String, Object> getDetails() {
      return details;
    }

    public void setDetails(Map<String, Object> details) {
      this.details = details;
    }
  }
}

