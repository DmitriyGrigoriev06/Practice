package com.courserec.rating.repository;

import com.courserec.rating.model.Rating;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
  Optional<Rating> findByUserIdAndCourseId(UUID userId, UUID courseId);

  Page<Rating> findByUserId(UUID userId, Pageable pageable);

  Page<Rating> findByCourseId(UUID courseId, Pageable pageable);

  @Query("SELECT r FROM Rating r WHERE (:userId IS NULL OR r.userId = :userId) AND (:courseId IS NULL OR r.courseId = :courseId)")
  Page<Rating> findByUserIdAndCourseId(
      @Param("userId") UUID userId, @Param("courseId") UUID courseId, Pageable pageable);

  List<Rating> findByUserId(UUID userId);
}

