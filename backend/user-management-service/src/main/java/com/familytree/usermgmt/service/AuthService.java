package com.familytree.usermgmt.service;

import com.familytree.usermgmt.dto.AuthResponse;
import com.familytree.usermgmt.dto.LoginRequest;
import com.familytree.usermgmt.dto.RegisterRequest;
import com.familytree.usermgmt.dto.SocialAuthRequest;
import com.familytree.usermgmt.exception.NotFoundException;
import com.familytree.usermgmt.exception.ValidationException;
import com.familytree.usermgmt.model.AuthProvider;
import com.familytree.usermgmt.model.UserEntity;
import com.familytree.usermgmt.repository.JpaUserRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final JpaUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(JpaUserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String username = request.username().trim().toLowerCase(Locale.ROOT);
    if (userRepository.findByUsername(username).isPresent()) {
      throw new ValidationException("Username already taken");
    }

    Instant now = Instant.now();
    UserEntity user = new UserEntity(
        UUID.randomUUID().toString(),
        username,
        passwordEncoder.encode(request.password()),
        request.email().trim().toLowerCase(Locale.ROOT),
        request.displayName().trim(),
        AuthProvider.LOCAL.name(),
        null,
        null,
        now,
        now);

    userRepository.save(user);
    return toAuthResponse(user);
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    String username = request.username().trim().toLowerCase(Locale.ROOT);
    UserEntity user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ValidationException("Invalid username or password"));

    if (!AuthProvider.LOCAL.name().equals(user.getAuthProvider())) {
      throw new ValidationException("This account uses social login");
    }

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new ValidationException("Invalid username or password");
    }

    return toAuthResponse(user);
  }

  @Transactional
  public AuthResponse createOrSignIn(SocialAuthRequest request) {
    String providerName = request.provider().trim().toUpperCase(Locale.ROOT);

    // FACEBOOK and GOOGLE are documented as future use; only GMAIL works for now
    if (providerName.equals("FACEBOOK") || providerName.equals("GOOGLE")) {
      throw new ValidationException("Social login for " + providerName + " is not yet implemented");
    }

    AuthProvider provider = AuthProvider.fromInput(providerName);
    String subject = request.providerUserId().trim();
    String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
    String normalizedDisplayName = request.displayName().trim();

    UserEntity user = userRepository
        .findByAuthProviderAndProviderUserId(provider.name(), subject)
        .orElse(null);

    if (user == null) {
      Instant now = Instant.now();
      user = new UserEntity(
          UUID.randomUUID().toString(),
          null,
          null,
          normalizedEmail,
          normalizedDisplayName,
          provider.name(),
          subject,
          null,
          now,
          now);
    } else {
      user.setEmail(normalizedEmail);
      user.setDisplayName(normalizedDisplayName);
      user.setUpdatedAt(Instant.now());
    }

    userRepository.save(user);
    return toAuthResponse(user);
  }

  private AuthResponse toAuthResponse(UserEntity user) {
    String accessToken = "dev-access-" + user.getId();
    String refreshToken = "dev-refresh-" + user.getId();
    return new AuthResponse(
        user.getId(),
        accessToken,
        refreshToken,
        user.getDisplayName(),
        user.getEmail(),
        user.getAuthProvider(),
        user.getFamilyId());
  }
}
