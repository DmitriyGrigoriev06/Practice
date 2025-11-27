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
public class UserServiceClient {
  private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
  private final WebClient webClient;
  private final String userServiceUrl;
  private final CacheService cacheService;

  public UserServiceClient(
      WebClient.Builder webClientBuilder,
      @Value("${services.user-service.url}") String userServiceUrl,
      CacheService cacheService) {
    this.userServiceUrl = userServiceUrl;
    this.webClient = webClientBuilder.baseUrl(userServiceUrl).build();
    this.cacheService = cacheService;
  }

  public boolean validateUser(UUID userId, String jwtToken) {
    // Check cache first
    Boolean cached = cacheService.getUserValidation(userId);
    if (cached != null) {
      logger.debug("User validation cache hit for userId: {}", userId);
      return cached;
    }
    try {
      var requestSpec = webClient.get().uri("/api/v1/users/{userId}", userId);
      
      // Add JWT token if provided
      if (jwtToken != null && !jwtToken.isEmpty()) {
        requestSpec = requestSpec.header("Authorization", "Bearer " + jwtToken);
      }
      
      UserResponse response =
          requestSpec
              .retrieve()
              .onStatus(
                  status -> status.is4xxClientError(),
                  clientResponse -> {
                    logger.warn("User validation failed for userId: {} - status: {}", userId, clientResponse.statusCode());
                    return Mono.error(new IllegalArgumentException("User not found"));
                  })
              .bodyToMono(UserResponse.class)
              .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(1)))
              .block();

      boolean isValid =
          response != null
              && response.getAccountStatus() != null
              && response.getAccountStatus().equals("ACTIVE");
      // Cache the result
      cacheService.putUserValidation(userId, isValid);
      return isValid;
    } catch (Exception e) {
      logger.error("Error validating user {}: {}", userId, e.getMessage());
      cacheService.putUserValidation(userId, false);
      return false;
    }
  }

  private static class UserResponse {
    private String accountStatus;

    public String getAccountStatus() {
      return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
      this.accountStatus = accountStatus;
    }
  }
}

