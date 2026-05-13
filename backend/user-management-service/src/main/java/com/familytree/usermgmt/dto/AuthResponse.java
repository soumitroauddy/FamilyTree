package com.familytree.usermgmt.dto;

public record AuthResponse(
    String userId,
    String accessToken,
    String refreshToken,
    String displayName,
    String email,
    String provider,
    String familyId
) {
}
