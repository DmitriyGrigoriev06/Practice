package com.courserec.course.service;

import com.courserec.course.model.Course;
import com.courserec.course.repository.CourseRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {
  private final CourseRepository courseRepository;

  public CourseService(CourseRepository courseRepository) {
    this.courseRepository = courseRepository;
  }

  public Page<Course> getAllCourses(String category, Pageable pageable) {
    if (category != null && !category.isEmpty()) {
      return courseRepository.findByDeletedFalseAndCategory(category, pageable);
    }
    return courseRepository.findByDeletedFalse(pageable);
  }

  public Course getCourseById(UUID courseId) {
    return courseRepository
        .findByIdAndDeletedFalse(courseId)
        .orElseThrow(() -> new IllegalArgumentException("Course not found"));
  }

  public List<Course> getCoursesByIds(List<UUID> courseIds) {
    return courseRepository.findByIdInAndDeletedFalse(courseIds);
  }

  @Transactional
  public Course createCourse(Course course) {
    return courseRepository.save(course);
  }

  @Transactional
  public Course updateCourse(UUID courseId, Course updatedCourse) {
    Course existingCourse = getCourseById(courseId);
    existingCourse.setTitle(updatedCourse.getTitle());
    existingCourse.setDescription(updatedCourse.getDescription());
    existingCourse.setCategory(updatedCourse.getCategory());
    return courseRepository.save(existingCourse);
  }

  @Transactional
  public void softDeleteCourse(UUID courseId) {
    Course course = getCourseById(courseId);
    course.setDeleted(true);
    courseRepository.save(course);
  }
}

