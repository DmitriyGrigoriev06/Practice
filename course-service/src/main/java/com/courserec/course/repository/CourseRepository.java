package com.courserec.course.repository;

import com.courserec.course.model.Course;
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
public interface CourseRepository extends JpaRepository<Course, UUID> {
  Page<Course> findByDeletedFalse(Pageable pageable);

  @Query("SELECT c FROM Course c WHERE c.deleted = false AND (:category IS NULL OR c.category = :category)")
  Page<Course> findByDeletedFalseAndCategory(
      @Param("category") String category, Pageable pageable);

  Optional<Course> findByIdAndDeletedFalse(UUID id);

  @Query("SELECT c FROM Course c WHERE c.deleted = false AND c.id IN :ids")
  List<Course> findByIdInAndDeletedFalse(@Param("ids") List<UUID> ids);
}

