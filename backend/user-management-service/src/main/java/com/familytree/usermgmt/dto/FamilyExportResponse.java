package com.familytree.usermgmt.dto;

import java.time.Instant;
import java.util.List;

public record FamilyExportResponse(
    String familyId,
    String familyName,
    Instant exportedAt,
    List<FamilyMemberResponse> members
) {}
