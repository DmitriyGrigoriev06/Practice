package com.courserec.rating.unit.service;

import com.courserec.rating.client.CourseServiceClient;
import com.courserec.rating.client.UserServiceClient;
import com.courserec.rating.kafka.RatingEventProducer;
import com.courserec.rating.model.Rating;
import com.courserec.rating.repository.RatingRepository;
import com.courserec.rating.service.RatingService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {
  @Mock private RatingRepository ratingRepository;
  @Mock private UserServiceClient userServiceClient;
  @Mock private CourseServiceClient courseServiceClient;
  @Mock private RatingEventProducer ratingEventProducer;

  @InjectMocks private RatingService ratingService;

  private UUID userId;
  private UUID courseId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    courseId = UUID.randomUUID();
  }

  @Test
  void testSubmitRating() {
    String jwtToken = "test-token";
    when(userServiceClient.validateUser(userId, jwtToken)).thenReturn(true);
    when(courseServiceClient.validateCourse(courseId, jwtToken)).thenReturn(true);
    when(ratingRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());
    when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Rating result = ratingService.submitRating(userId, courseId, 5, jwtToken);

    assertNotNull(result);
    assertEquals(userId, result.getUserId());
    assertEquals(courseId, result.getCourseId());
    assertEquals(5, result.getRatingValue());
    verify(ratingEventProducer).publishRatingSubmittedEvent(any());
  }

  @Test
  void testSubmitRatingInvalidValue() {
    String jwtToken = "test-token";
    assertThrows(IllegalArgumentException.class, () -> {
      ratingService.submitRating(userId, courseId, 6, jwtToken);
    });

    verify(ratingRepository, never()).save(any());
  }

  @Test
  void testSubmitRatingUserNotFound() {
    String jwtToken = "test-token";
    when(userServiceClient.validateUser(userId, jwtToken)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> {
      ratingService.submitRating(userId, courseId, 5, jwtToken);
    });

    verify(ratingRepository, never()).save(any());
  }

  @Test
  void testSubmitRatingUpsert() {
    String jwtToken = "test-token";
    Rating existingRating = new Rating();
    existingRating.setUserId(userId);
    existingRating.setCourseId(courseId);
    existingRating.setRatingValue(3);

    when(userServiceClient.validateUser(userId, jwtToken)).thenReturn(true);
    when(courseServiceClient.validateCourse(courseId, jwtToken)).thenReturn(true);
    when(ratingRepository.findByUserIdAndCourseId(userId, courseId))
        .thenReturn(Optional.of(existingRating));
    when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Rating result = ratingService.submitRating(userId, courseId, 5, jwtToken);

    assertEquals(5, result.getRatingValue());
    verify(ratingRepository).save(existingRating);
  }
}

