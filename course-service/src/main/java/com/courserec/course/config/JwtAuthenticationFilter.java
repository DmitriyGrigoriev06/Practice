package com.courserec.course.config;

import com.courserec.course.security.JwtTokenValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final JwtTokenValidator jwtTokenValidator;

  public JwtAuthenticationFilter(JwtTokenValidator jwtTokenValidator) {
    this.jwtTokenValidator = jwtTokenValidator;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = getTokenFromRequest(request);

    if (token != null) {
      try {
        if (jwtTokenValidator.validateToken(token)) {
          String role = jwtTokenValidator.getRoleFromToken(token);
          String userId = jwtTokenValidator.getUserIdFromToken(token);

          logger.debug("JWT token validated - userId: {}, role: {}", userId, role);

          if (role != null && userId != null) {
            // Normalize role to uppercase to match enum values
            String normalizedRole = role.toUpperCase();
            String authority = "ROLE_" + normalizedRole;
            
            logger.debug("Setting authentication with authority: {}", authority);
            
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userId, null, java.util.Collections.singletonList(new SimpleGrantedAuthority(authority)));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("Authentication set successfully for userId: {}", userId);
          } else {
            logger.warn("JWT token missing role or userId - role: {}, userId: {}", role, userId);
          }
        } else {
          logger.debug("JWT token validation failed - token is invalid or expired");
        }
      } catch (Exception e) {
        // Token validation failed - clear security context
        SecurityContextHolder.clearContext();
        // Log error but continue filter chain
        logger.warn("JWT token validation failed: {}", e.getMessage(), e);
      }
    } else {
      logger.debug("No JWT token found in request");
    }

    filterChain.doFilter(request, response);
  }

  private String getTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    logger.debug("Authorization header: {}", bearerToken != null ? "present" : "missing");
    if (bearerToken != null) {
      logger.debug("Authorization header value starts with Bearer: {}", bearerToken.startsWith("Bearer "));
      if (bearerToken.startsWith("Bearer ")) {
        String token = bearerToken.substring(7);
        logger.debug("Extracted token length: {}", token.length());
        return token;
      } else {
        logger.warn("Authorization header does not start with 'Bearer ': {}", bearerToken.substring(0, Math.min(20, bearerToken.length())));
      }
    } else {
      logger.debug("No Authorization header found in request");
    }
    return null;
  }
}

