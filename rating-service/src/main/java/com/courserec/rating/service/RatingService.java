package com.courserec.rating.service;

import com.courserec.rating.client.CourseServiceClient;
import com.courserec.rating.client.UserServiceClient;
import com.courserec.rating.kafka.RatingEventProducer;
import com.courserec.rating.kafka.RatingSubmittedEvent;
import com.courserec.rating.model.Rating;
import com.courserec.rating.repository.RatingRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingService {
  private static final Logger logger = LoggerFactory.getLogger(RatingService.class);
  private final RatingRepository ratingRepository;
  private final UserServiceClient userServiceClient;
  private final CourseServiceClient courseServiceClient;
  private final RatingEventProducer ratingEventProducer;

  public RatingService(
      RatingRepository ratingRepository,
      UserServiceClient userServiceClient,
      CourseServiceClient courseServiceClient,
      RatingEventProducer ratingEventProducer) {
    this.ratingRepository = ratingRepository;
    this.userServiceClient = userServiceClient;
    this.courseServiceClient = courseServiceClient;
    this.ratingEventProducer = ratingEventProducer;
  }

  @Transactional
  public Rating submitRating(UUID userId, UUID courseId, Integer ratingValue, String jwtToken) {
    // Validate rating value
    if (ratingValue < 1 || ratingValue > 5) {
      throw new IllegalArgumentException("Rating value must be between 1 and 5");
    }

    // Validate user exists and is active
    if (!userServiceClient.validateUser(userId, jwtToken)) {
      throw new IllegalArgumentException("User not found or inactive");
    }

    // Validate course exists and is not deleted
    if (!courseServiceClient.validateCourse(courseId, jwtToken)) {
      throw new IllegalArgumentException("Course not found or deleted");
    }

    // Check if rating already exists (upsert logic)
    Rating rating =
        ratingRepository
            .findByUserIdAndCourseId(userId, courseId)
            .orElse(new Rating());

    // Last-write-wins: update existing rating or create new one
    rating.setUserId(userId);
    rating.setCourseId(courseId);
    rating.setRatingValue(ratingValue);

    Rating savedRating = ratingRepository.save(rating);

    // Publish event to Kafka
    try {
      RatingSubmittedEvent event =
          new RatingSubmittedEvent(
              savedRating.getId(),
              savedRating.getUserId(),
              savedRating.getCourseId(),
              savedRating.getRatingValue());
      ratingEventProducer.publishRatingSubmittedEvent(event);
      logger.info(
          "Rating submitted and event published - ratingId: {}, userId: {}, courseId: {}, ratingValue: {}",
          savedRating.getId(),
          userId,
          courseId,
          ratingValue);
    } catch (Exception e) {
      logger.error("Failed to publish rating event: {}", e.getMessage(), e);
      // Don't fail the transaction if event publishing fails
    }

    return savedRating;
  }

  public Page<Rating> getRatings(UUID userId, UUID courseId, Pageable pageable) {
    if (userId != null && courseId != null) {
      return ratingRepository.findByUserIdAndCourseId(userId, courseId, pageable);
    } else if (userId != null) {
      return ratingRepository.findByUserId(userId, pageable);
    } else if (courseId != null) {
      return ratingRepository.findByCourseId(courseId, pageable);
    }
    return ratingRepository.findAll(pageable);
  }

  public Rating getRatingById(UUID ratingId) {
    return ratingRepository
        .findById(ratingId)
        .orElseThrow(() -> new IllegalArgumentException("Rating not found"));
  }

  public List<Rating> getRatingsByUserId(UUID userId) {
    return ratingRepository.findByUserId(userId);
  }
}

