package com.courserec.rating.client;

import com.courserec.rating.service.CacheService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class CourseServiceClient {
  private static final Logger logger = LoggerFactory.getLogger(CourseServiceClient.class);
  private final WebClient webClient;
  private final String courseServiceUrl;
  private final CacheService cacheService;

  public CourseServiceClient(
      WebClient.Builder webClientBuilder,
      @Value("${services.course-service.url}") String courseServiceUrl,
      CacheService cacheService) {
    this.courseServiceUrl = courseServiceUrl;
    this.webClient = webClientBuilder.baseUrl(courseServiceUrl).build();
    this.cacheService = cacheService;
  }

  public boolean validateCourse(UUID courseId, String jwtToken) {
    // Check cache first
    Boolean cached = cacheService.getCourseValidation(courseId);
    if (cached != null) {
      logger.debug("Course validation cache hit for courseId: {}", courseId);
      return cached;
    }
    try {
      var requestSpec = webClient.get().uri("/api/v1/courses/{courseId}", courseId);
      
      // Add JWT token if provided
      if (jwtToken != null && !jwtToken.isEmpty()) {
        requestSpec = requestSpec.header("Authorization", "Bearer " + jwtToken);
      }
      
      CourseResponse response =
          requestSpec
              .retrieve()
              .onStatus(
                  status -> status.is4xxClientError(),
                  clientResponse -> {
                    logger.warn("Course validation failed for courseId: {} - status: {}", courseId, clientResponse.statusCode());
                    return Mono.error(new IllegalArgumentException("Course not found"));
                  })
              .bodyToMono(CourseResponse.class)
              .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(1)))
              .block();

      boolean isValid = response != null;
      // Cache the result
      cacheService.putCourseValidation(courseId, isValid);
      return isValid;
    } catch (Exception e) {
      logger.error("Error validating course {}: {}", courseId, e.getMessage());
      cacheService.putCourseValidation(courseId, false);
      return false;
    }
  }

  private static class CourseResponse {
    private UUID id;

    public UUID getId() {
      return id;
    }

    public void setId(UUID id) {
      this.id = id;
    }
  }
}

