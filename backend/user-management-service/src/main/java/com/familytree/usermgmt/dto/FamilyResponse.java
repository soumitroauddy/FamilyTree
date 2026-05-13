package com.familytree.usermgmt.dto;

import com.familytree.usermgmt.model.Family;
import java.time.Instant;
import java.util.Set;

public record FamilyResponse(
    String id,
    String name,
    String joinCode,
    String ownerUserId,
    Set<String> memberUserIds,
    Instant createdAt) {

  public static FamilyResponse from(Family family) {
    return new FamilyResponse(
        family.id(),
        family.name(),
        family.joinCode(),
        family.ownerUserId(),
        family.memberUserIds(),
        family.createdAt());
  }
}
