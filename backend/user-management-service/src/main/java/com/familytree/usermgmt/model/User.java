package com.familytree.usermgmt.model;

import java.time.Instant;

public record User(
    String id,
    String email,
    String displayName,
    AuthProvider provider,
    String providerUserId,
    String familyId,
    Instant createdAt,
    Instant updatedAt
) {
  public User withFamilyId(String nextFamilyId) {
    return new User(
        id,
        email,
        displayName,
        provider,
        providerUserId,
        nextFamilyId,
        createdAt,
        Instant.now());
  }

  public User withProfile(String nextEmail, String nextDisplayName) {
    return new User(
        id,
        nextEmail,
        nextDisplayName,
        provider,
        providerUserId,
        familyId,
        createdAt,
        Instant.now());
  }
}
