package com.courserec.user.unit.security;

import com.courserec.user.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
  private JwtTokenProvider jwtTokenProvider;
  private static final String SECRET = "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256-algorithm";
  private static final long ACCESS_TOKEN_EXPIRATION = 900000; // 15 minutes
  private static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider(SECRET, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
  }

  @Test
  void testGenerateAccessToken() {
    String token = jwtTokenProvider.generateAccessToken("user-id", "user@example.com", "USER");

    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  void testValidateToken() {
    String token = jwtTokenProvider.generateAccessToken("user-id", "user@example.com", "USER");

    boolean isValid = jwtTokenProvider.validateToken(token);

    assertTrue(isValid);
  }

  @Test
  void testGetUserIdFromToken() {
    String userId = "user-id";
    String token = jwtTokenProvider.generateAccessToken(userId, "user@example.com", "USER");

    String extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

    assertEquals(userId, extractedUserId);
  }

  @Test
  void testGetRoleFromToken() {
    String role = "ADMIN";
    String token = jwtTokenProvider.generateAccessToken("user-id", "user@example.com", role);

    String extractedRole = jwtTokenProvider.getRoleFromToken(token);

    assertEquals(role, extractedRole);
  }
}

