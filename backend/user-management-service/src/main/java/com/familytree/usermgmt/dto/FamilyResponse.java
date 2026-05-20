package com.familytree.usermgmt.dto;

import java.time.Instant;
import java.util.Set;

public record FamilyResponse(
    String id,
    String name,
    String joinCode,
    String ownerUserId,
    Set<String> memberUserIds,
    Instant createdAt) {}
