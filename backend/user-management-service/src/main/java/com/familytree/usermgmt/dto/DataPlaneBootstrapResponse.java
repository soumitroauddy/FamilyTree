package com.familytree.usermgmt.dto;

public record DataPlaneBootstrapResponse(
    String userId,
    String displayName,
    String email,
    String provider,
    String familyId,
    String familyName
) {}
