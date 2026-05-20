package com.familytree.usermgmt.dto;

import com.familytree.usermgmt.model.FamilyMemberEntity;
import java.time.Instant;

public record FamilyMemberResponse(
    String id,
    String familyId,
    String addedByUserId,
    String fullName,
    String photoUrl,
    Integer birthYear,
    Integer deathYear,
    String bio,
    String location,
    String parentMemberId,
    Instant createdAt,
    Instant updatedAt
) {
  /** {@code photoUrlForClient} is a presigned download URL, or null when the member has no photo. */
  public static FamilyMemberResponse from(FamilyMemberEntity entity, String photoUrlForClient) {
    return new FamilyMemberResponse(
        entity.getId(),
        entity.getFamilyId(),
        entity.getAddedByUserId(),
        entity.getFullName(),
        photoUrlForClient,
        entity.getBirthYear(),
        entity.getDeathYear(),
        entity.getBio(),
        entity.getLocation(),
        entity.getParentMemberId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
