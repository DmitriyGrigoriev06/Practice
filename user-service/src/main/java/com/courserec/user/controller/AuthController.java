package com.courserec.user.controller;

import com.courserec.user.model.RefreshToken;
import com.courserec.user.model.User;
import com.courserec.user.model.User.Role;
import com.courserec.user.model.dto.AuthResponse;
import com.courserec.user.model.dto.ErrorResponse;
import com.courserec.user.model.dto.LoginRequest;
import com.courserec.user.model.dto.RefreshRequest;
import com.courserec.user.model.dto.RegisterRequest;
import com.courserec.user.repository.RefreshTokenRepository;
import com.courserec.user.security.JwtTokenProvider;
import com.courserec.user.service.PasswordEncoderService;
import com.courserec.user.service.UserService;
import jakarta.validation.Valid;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {
  private final UserService userService;
  private final PasswordEncoderService passwordEncoderService;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;

  public AuthController(
      UserService userService,
      PasswordEncoderService passwordEncoderService,
      JwtTokenProvider jwtTokenProvider,
      RefreshTokenRepository refreshTokenRepository) {
    this.userService = userService;
    this.passwordEncoderService = passwordEncoderService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @PostMapping("/register")
  @Operation(summary = "Register a new user", description = "Creates a new user account and returns JWT tokens")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "409", description = "Email already exists"),
        @ApiResponse(responseCode = "400", description = "Validation error")
      })
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    try {
      Role role = null;
      if (request.getRole() != null && !request.getRole().isEmpty()) {
        try {
          role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
          Map<String, Object> details = new HashMap<>();
          details.put("field", "role");
          details.put("rejectedValue", request.getRole());
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ErrorResponse("INVALID_ROLE", "Role must be USER or ADMIN", details));
        }
      }

      User user = userService.register(request.getEmail(), request.getPassword(), role);

      String accessToken = jwtTokenProvider.generateAccessToken(
          user.getId().toString(), user.getEmail(), user.getRole().name());
      String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString());

      // Store refresh token
      RefreshToken refreshTokenEntity = new RefreshToken();
      refreshTokenEntity.setUser(user);
      refreshTokenEntity.setTokenHash(hashToken(refreshToken));
      refreshTokenEntity.setExpiresAt(Instant.now().plusSeconds(604800)); // 7 days
      refreshTokenRepository.save(refreshTokenEntity);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new AuthResponse(accessToken, refreshToken));
    } catch (IllegalArgumentException e) {
      if (e.getMessage().contains("already exists")) {
        Map<String, Object> details = new HashMap<>();
        details.put("email", request.getEmail());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("DUPLICATE_EMAIL", "Email already registered", details));
      }
      Map<String, Object> details = new HashMap<>();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(), details));
    }
  }

  @PostMapping("/login")
  @Operation(summary = "Login user", description = "Authenticates user and returns JWT tokens")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "403", description = "Account inactive")
      })
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    try {
      User user = userService.findByEmail(request.getEmail());

      if (!passwordEncoderService.matches(request.getPassword(), user.getPasswordHash())) {
        Map<String, Object> details = new HashMap<>();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("INVALID_CREDENTIALS", "Invalid email or password", details));
      }

      if (user.getAccountStatus() != User.AccountStatus.ACTIVE) {
        Map<String, Object> details = new HashMap<>();
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("ACCOUNT_INACTIVE", "Account is not active", details));
      }

      String accessToken = jwtTokenProvider.generateAccessToken(
          user.getId().toString(), user.getEmail(), user.getRole().name());
      String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString());

      // Store refresh token
      RefreshToken refreshTokenEntity = new RefreshToken();
      refreshTokenEntity.setUser(user);
      refreshTokenEntity.setTokenHash(hashToken(refreshToken));
      refreshTokenEntity.setExpiresAt(Instant.now().plusSeconds(604800)); // 7 days
      refreshTokenRepository.save(refreshTokenEntity);

      return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    } catch (IllegalArgumentException e) {
      Map<String, Object> details = new HashMap<>();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ErrorResponse("INVALID_CREDENTIALS", "Invalid email or password", details));
    }
  }

  @PostMapping("/refresh")
  @Operation(summary = "Refresh access token", description = "Generates a new access token using refresh token")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token")
      })
  public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {
    try {
      String tokenHash = hashToken(request.getRefreshToken());
      RefreshToken refreshTokenEntity =
          refreshTokenRepository
              .findByTokenHash(tokenHash)
              .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

      if (refreshTokenEntity.getRevoked() || refreshTokenEntity.isExpired()) {
        Map<String, Object> details = new HashMap<>();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("INVALID_TOKEN", "Refresh token is invalid or expired", details));
      }

      User user = refreshTokenEntity.getUser();
      String accessToken = jwtTokenProvider.generateAccessToken(
          user.getId().toString(), user.getEmail(), user.getRole().name());

      return ResponseEntity.ok(new AuthResponse(accessToken, request.getRefreshToken()));
    } catch (Exception e) {
      Map<String, Object> details = new HashMap<>();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ErrorResponse("INVALID_TOKEN", "Refresh token is invalid", details));
    }
  }

  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes());
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (Exception e) {
      throw new RuntimeException("Error hashing token", e);
    }
  }
}

