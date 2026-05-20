package com.familytree.usermgmt.dto;

import java.time.Instant;

public record InvitationResponse(
    String id,
    String inviteCode,
    String familyId,
    String familyName,
    String inviterDisplayName,
    String inviteeEmail,
    String status,
    Instant createdAt,
    Instant expiresAt
) {}
