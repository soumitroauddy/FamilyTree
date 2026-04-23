package com.familytree.usermgmt.service;

import com.familytree.usermgmt.dto.AuthResponse;
import com.familytree.usermgmt.dto.SocialAuthRequest;
import com.familytree.usermgmt.model.AuthProvider;
import com.familytree.usermgmt.model.User;
import com.familytree.usermgmt.repository.UserRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepository;

  public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public AuthResponse createOrSignIn(SocialAuthRequest request) {
    AuthProvider provider =
        AuthProvider.valueOf(request.provider().trim().toUpperCase(Locale.ROOT));
    String subject = request.providerUserId().trim();

    String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
    String normalizedDisplayName = request.displayName().trim();

    User user =
        userRepository
            .findByProviderAndProviderUserId(provider, subject)
            .map(existing -> existing.withProfile(normalizedEmail, normalizedDisplayName))
            .orElseGet(
                () ->
                    new User(
                        UUID.randomUUID().toString(),
                        normalizedEmail,
                        normalizedDisplayName,
                        provider,
                        subject,
                        null,
                        Instant.now(),
                        Instant.now()));

    userRepository.save(user);

    String accessToken = "dev-access-" + user.id();
    String refreshToken = "dev-refresh-" + user.id();
    return new AuthResponse(
        user.id(),
        user.displayName(),
        user.email(),
        user.provider().name(),
        accessToken,
        refreshToken,
        user.familyId());
  }
}
