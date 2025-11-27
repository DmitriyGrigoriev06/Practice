package com.courserec.rating.controller;

import com.courserec.rating.model.Rating;
import com.courserec.rating.model.dto.ErrorResponse;
import com.courserec.rating.model.dto.RatingPageResponse;
import com.courserec.rating.model.dto.RatingResponse;
import com.courserec.rating.model.dto.SubmitRatingRequest;
import com.courserec.rating.security.JwtTokenValidator;
import com.courserec.rating.service.RatingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ratings")
@Tag(name = "Ratings", description = "Rating submission and retrieval endpoints")
public class RatingController {
  private static final Logger logger = LoggerFactory.getLogger(RatingController.class);
  private final RatingService ratingService;
  private final JwtTokenValidator jwtTokenValidator;

  public RatingController(RatingService ratingService, JwtTokenValidator jwtTokenValidator) {
    this.ratingService = ratingService;
    this.jwtTokenValidator = jwtTokenValidator;
  }

  @PostMapping
  @Operation(summary = "Submit rating", description = "Submits or updates a rating for a course")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Rating submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid rating value"),
        @ApiResponse(responseCode = "404", description = "Course not found")
      })
  public ResponseEntity<?> submitRating(
      @Valid @RequestBody SubmitRatingRequest request, HttpServletRequest httpRequest) {
    String token = getTokenFromRequest(httpRequest);
    String userId = jwtTokenValidator.getUserIdFromToken(token);
    UUID userIdUuid = UUID.fromString(userId);

    logger.info(
        "Submitting rating - userId: {}, courseId: {}, ratingValue: {}",
        userId,
        request.getCourseId(),
        request.getRatingValue());

    try {
      Rating rating =
          ratingService.submitRating(userIdUuid, request.getCourseId(), request.getRatingValue(), token);
      return ResponseEntity.status(HttpStatus.CREATED).body(new RatingResponse(rating));
    } catch (IllegalArgumentException e) {
      Map<String, Object> details = new HashMap<>();
      if (e.getMessage().contains("not found")) {
        details.put("courseId", request.getCourseId().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("COURSE_NOT_FOUND", e.getMessage(), details));
      } else if (e.getMessage().contains("inactive")) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("USER_INACTIVE", e.getMessage(), details));
      } else if (e.getMessage().contains("between 1 and 5")) {
        details.put("ratingValue", request.getRatingValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("INVALID_RATING_VALUE", e.getMessage(), details));
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(), details));
    }
  }

  @GetMapping
  public ResponseEntity<RatingPageResponse> getRatings(
      @RequestParam(required = false) UUID userId,
      @RequestParam(required = false) UUID courseId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    logger.info("Getting ratings - userId: {}, courseId: {}, page: {}, size: {}", userId, courseId, page, size);
    try {
      Pageable pageable = PageRequest.of(page, size);
      Page<Rating> ratingsPage = ratingService.getRatings(userId, courseId, pageable);

      List<RatingResponse> content =
          ratingsPage.getContent().stream()
              .map(RatingResponse::new)
              .collect(Collectors.toList());

      RatingPageResponse response =
          new RatingPageResponse(
              content,
              ratingsPage.getNumber(),
              ratingsPage.getSize(),
              ratingsPage.getTotalElements(),
              ratingsPage.getTotalPages());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Error getting ratings: {}", e.getMessage(), e);
      Map<String, Object> details = new HashMap<>();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(null);
    }
  }

  @GetMapping("/{ratingId}")
  public ResponseEntity<?> getRating(@PathVariable UUID ratingId) {
    logger.info("Getting rating by id: {}", ratingId);
    try {
      Rating rating = ratingService.getRatingById(ratingId);
      return ResponseEntity.ok(new RatingResponse(rating));
    } catch (IllegalArgumentException e) {
      Map<String, Object> details = new HashMap<>();
      details.put("ratingId", ratingId.toString());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ErrorResponse("RATING_NOT_FOUND", "Rating not found", details));
    }
  }

  private String getTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}

