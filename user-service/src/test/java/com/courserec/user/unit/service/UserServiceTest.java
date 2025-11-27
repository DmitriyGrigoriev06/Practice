package com.courserec.user.unit.service;

import com.courserec.user.model.User;
import com.courserec.user.model.User.Role;
import com.courserec.user.repository.UserRepository;
import com.courserec.user.service.PasswordEncoderService;
import com.courserec.user.service.UserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoderService passwordEncoderService;

  @InjectMocks private UserService userService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(UUID.randomUUID());
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("hashedPassword");
    testUser.setRole(Role.USER);
  }

  @Test
  void testRegisterNewUser() {
    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(passwordEncoderService.encode("password123")).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.register("test@example.com", "password123", Role.USER);

    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
    verify(userRepository).existsByEmail("test@example.com");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void testRegisterDuplicateEmail() {
    when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> {
      userService.register("test@example.com", "password123", Role.USER);
    });

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testFindByEmail() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    User result = userService.findByEmail("test@example.com");

    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
  }

  @Test
  void testFindByEmailNotFound() {
    when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> {
      userService.findByEmail("notfound@example.com");
    });
  }

  @Test
  void testValidatePassword() {
    when(passwordEncoderService.matches("password123", "hashedPassword")).thenReturn(true);

    boolean result = userService.validatePassword("password123", "hashedPassword");

    assertTrue(result);
    verify(passwordEncoderService).matches("password123", "hashedPassword");
  }
}

