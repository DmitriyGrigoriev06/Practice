package com.courserec.user.controller;

import com.courserec.user.model.User;
import com.courserec.user.model.dto.ErrorResponse;
import com.courserec.user.model.dto.UserResponse;
import com.courserec.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/{userId}")
  @Operation(summary = "Get user by ID", description = "Retrieves user information by user ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  public ResponseEntity<?> getUser(@PathVariable UUID userId) {
    try {
      User user = userService.findById(userId);
      return ResponseEntity.ok(new UserResponse(user));
    } catch (IllegalArgumentException e) {
      Map<String, Object> details = new HashMap<>();
      details.put("userId", userId.toString());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ErrorResponse("USER_NOT_FOUND", "User not found", details));
    }
  }
}

