package com.courserec.course.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenValidator {
  private static final Logger logger = LoggerFactory.getLogger(JwtTokenValidator.class);
  private final SecretKey secretKey;

  public JwtTokenValidator(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    logger.debug("JwtTokenValidator initialized with secret length: {}", secret.length());
  }

  public boolean validateToken(String token) {
    try {
      Claims claims = getClaimsFromToken(token);
      boolean isValid = !claims.getExpiration().before(new Date());
      if (!isValid) {
        logger.warn("Token expired. Expiration: {}, Current: {}", claims.getExpiration(), new Date());
      }
      return isValid;
    } catch (io.jsonwebtoken.security.SignatureException e) {
      logger.warn("JWT signature validation failed: {}", e.getMessage());
      return false;
    } catch (io.jsonwebtoken.ExpiredJwtException e) {
      logger.warn("JWT token expired: {}", e.getMessage());
      return false;
    } catch (Exception e) {
      logger.warn("JWT token validation error: {} - {}", e.getClass().getSimpleName(), e.getMessage());
      return false;
    }
  }

  public Claims getClaimsFromToken(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String getUserIdFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("userId", String.class);
  }

  public String getRoleFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("role", String.class);
  }
}

