package com.familytree.usermgmt.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record Family(
    String id,
    String name,
    String joinCode,
    String ownerUserId,
    Set<String> memberUserIds,
    Instant createdAt
) {
  public Family {
    memberUserIds = Collections.unmodifiableSet(new LinkedHashSet<>(memberUserIds));
  }
}
