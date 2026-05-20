package com.familytree.usermgmt.service;

import com.familytree.usermgmt.model.AuthProvider;
import com.familytree.usermgmt.model.UserEntity;
import com.familytree.usermgmt.repository.JpaUserRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures a local UserEntity row exists for every Supabase-authenticated user.
 * Called by SupabaseJwtFilter on each authenticated request. The upsert is a
 * no-op after the first call for a given userId.
 */
@Service
public class UserSyncService {

  private final JpaUserRepository userRepository;

  public UserSyncService(JpaUserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public UserEntity syncUser(String userId, String email, String displayName) {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("userId must not be blank");
    }
    return userRepository.findById(userId).orElseGet(() -> {
      String resolvedDisplay = (displayName != null && !displayName.isBlank())
          ? displayName.trim()
          : (email != null ? email.split("@")[0] : userId);
      String resolvedEmail = email != null ? email.toLowerCase().trim() : "";
      Instant now = Instant.now();
      UserEntity user = new UserEntity(
          userId,
          null,
          null,
          resolvedEmail,
          resolvedDisplay,
          AuthProvider.SUPABASE.name(),
          userId,
          null,
          now,
          now);
      return userRepository.save(user);
    });
  }
}
