package com.courserec.course.controller;

import com.courserec.course.model.Course;
import com.courserec.course.model.dto.CoursePageResponse;
import com.courserec.course.model.dto.CourseResponse;
import com.courserec.course.model.dto.CreateCourseRequest;
import com.courserec.course.model.dto.ErrorResponse;
import com.courserec.course.model.dto.UpdateCourseRequest;
import com.courserec.course.security.JwtTokenValidator;
import com.courserec.course.service.CourseService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Courses", description = "Course management endpoints")
public class CourseController {
  private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
  private final CourseService courseService;
  private final JwtTokenValidator jwtTokenValidator;

  public CourseController(CourseService courseService, JwtTokenValidator jwtTokenValidator) {
    this.courseService = courseService;
    this.jwtTokenValidator = jwtTokenValidator;
  }

  @GetMapping
  @Operation(summary = "Get courses", description = "Retrieves paginated list of courses with optional filtering")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Courses retrieved successfully")})
  public ResponseEntity<CoursePageResponse> getCourses(
      @RequestParam(required = false) String category,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "title,asc") String sort) {
    logger.info("Getting courses - category: {}, page: {}, size: {}, sort: {}", category, page, size, sort);
    try {
      String[] sortParams = sort.split(",");
      String sortBy = sortParams[0];
      Sort.Direction direction =
          sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
              ? Sort.Direction.DESC
              : Sort.Direction.ASC;

      Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
      Page<Course> coursesPage = courseService.getAllCourses(category, pageable);

      List<CourseResponse> content =
          coursesPage.getContent().stream()
              .map(CourseResponse::new)
              .collect(Collectors.toList());

      CoursePageResponse response =
          new CoursePageResponse(
              content,
              coursesPage.getNumber(),
              coursesPage.getSize(),
              coursesPage.getTotalElements(),
              coursesPage.getTotalPages());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> details = new HashMap<>();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(null);
    }
  }

  @GetMapping("/{courseId}")
  public ResponseEntity<?> getCourse(@PathVariable UUID courseId) {
    logger.info("Getting course by id: {}", courseId);
    try {
      Course course = courseService.getCourseById(courseId);
      return ResponseEntity.ok(new CourseResponse(course));
    } catch (IllegalArgumentException e) {
      Map<String, Object> details = new HashMap<>();
      details.put("courseId", courseId.toString());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ErrorResponse("COURSE_NOT_FOUND", "Course not found", details));
    }
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create course", description = "Creates a new course (Admin only)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Course created successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
      })
  public ResponseEntity<?> createCourse(
      @Valid @RequestBody CreateCourseRequest request, HttpServletRequest httpRequest) {
    String token = getTokenFromRequest(httpRequest);
    String userId = jwtTokenValidator.getUserIdFromToken(token);
    logger.info("Creating course - title: {}, userId: {}", request.getTitle(), userId);
    try {

      Course course = new Course();
      course.setTitle(request.getTitle());
      course.setDescription(request.getDescription());
      course.setCategory(request.getCategory());
      course.setCreatedBy(UUID.fromString(userId));

      Course createdCourse = courseService.createCourse(course);
      return ResponseEntity.status(HttpStatus.CREATED).body(new CourseResponse(createdCourse));
    } catch (Exception e) {
      Map<String, Object> details = new HashMap<>();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(), details));
    }
  }

  @PutMapping("/{courseId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> updateCourse(
      @PathVariable UUID courseId,
      @Valid @RequestBody UpdateCourseRequest request) {
    logger.info("Updating course - courseId: {}", courseId);
    try {
      Course updatedCourse = new Course();
      if (request.getTitle() != null) {
        updatedCourse.setTitle(request.getTitle());
      }
      if (request.getDescription() != null) {
        updatedCourse.setDescription(request.getDescription());
      }
      if (request.getCategory() != null) {
        updatedCourse.setCategory(request.getCategory());
      }

      Course course = courseService.updateCourse(courseId, updatedCourse);
      return ResponseEntity.ok(new CourseResponse(course));
    } catch (IllegalArgumentException e) {
      Map<String, Object> details = new HashMap<>();
      details.put("courseId", courseId.toString());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ErrorResponse("COURSE_NOT_FOUND", "Course not found", details));
    } catch (Exception e) {
      Map<String, Object> details = new HashMap<>();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(), details));
    }
  }

  @DeleteMapping("/{courseId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> deleteCourse(@PathVariable UUID courseId) {
    logger.info("Soft deleting course - courseId: {}", courseId);
    try {
      courseService.softDeleteCourse(courseId);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      Map<String, Object> details = new HashMap<>();
      details.put("courseId", courseId.toString());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ErrorResponse("COURSE_NOT_FOUND", "Course not found", details));
    }
  }

  @GetMapping("/batch")
  public ResponseEntity<?> getCoursesBatch(@RequestParam List<UUID> ids) {
    logger.info("Getting courses batch - ids count: {}", ids.size());
    try {
      List<Course> courses = courseService.getCoursesByIds(ids);
      List<CourseResponse> responses =
          courses.stream().map(CourseResponse::new).collect(Collectors.toList());
      return ResponseEntity.ok(responses);
    } catch (Exception e) {
      Map<String, Object> details = new HashMap<>();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse("BAD_REQUEST", e.getMessage(), details));
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

