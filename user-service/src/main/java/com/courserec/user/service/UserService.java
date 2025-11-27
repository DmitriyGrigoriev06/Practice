package com.courserec.user.service;

import com.courserec.user.model.User;
import com.courserec.user.model.User.AccountStatus;
import com.courserec.user.model.User.Role;
import com.courserec.user.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoderService passwordEncoderService;

  public UserService(UserRepository userRepository, PasswordEncoderService passwordEncoderService) {
    this.userRepository = userRepository;
    this.passwordEncoderService = passwordEncoderService;
  }

  @Transactional
  public User register(String email, String password, Role role) {
    if (userRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Email already exists");
    }

    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(passwordEncoderService.encode(password));
    user.setRole(role != null ? role : Role.USER);
    user.setAccountStatus(AccountStatus.ACTIVE);

    return userRepository.save(user);
  }

  public User findByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  public User findById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  public boolean validatePassword(String rawPassword, String encodedPassword) {
    return passwordEncoderService.matches(rawPassword, encodedPassword);
  }
}

