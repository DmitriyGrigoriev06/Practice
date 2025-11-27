package com.courserec.course.unit.service;

import com.courserec.course.model.Course;
import com.courserec.course.repository.CourseRepository;
import com.courserec.course.service.CourseService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
  @Mock private CourseRepository courseRepository;

  @InjectMocks private CourseService courseService;

  private Course testCourse;

  @BeforeEach
  void setUp() {
    testCourse = new Course();
    testCourse.setId(UUID.randomUUID());
    testCourse.setTitle("Test Course");
    testCourse.setDescription("Test Description");
    testCourse.setCategory("Programming");
    testCourse.setDeleted(false);
  }

  @Test
  void testGetCourseById() {
    UUID courseId = testCourse.getId();
    when(courseRepository.findByIdAndDeletedFalse(courseId))
        .thenReturn(Optional.of(testCourse));

    Course result = courseService.getCourseById(courseId);

    assertNotNull(result);
    assertEquals(courseId, result.getId());
    verify(courseRepository).findByIdAndDeletedFalse(courseId);
  }

  @Test
  void testGetCourseByIdNotFound() {
    UUID courseId = UUID.randomUUID();
    when(courseRepository.findByIdAndDeletedFalse(courseId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> {
      courseService.getCourseById(courseId);
    });
  }

  @Test
  void testCreateCourse() {
    when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

    Course result = courseService.createCourse(testCourse);

    assertNotNull(result);
    verify(courseRepository).save(testCourse);
  }

  @Test
  void testSoftDeleteCourse() {
    UUID courseId = testCourse.getId();
    when(courseRepository.findByIdAndDeletedFalse(courseId))
        .thenReturn(Optional.of(testCourse));
    when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

    courseService.softDeleteCourse(courseId);

    assertTrue(testCourse.getDeleted());
    verify(courseRepository).save(testCourse);
  }
}

