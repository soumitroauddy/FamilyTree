package com.familytree.usermgmt.dto;

public record ControlPlaneUserResponse(
    String userId,
    String email,
    String displayName,
    String provider,
    String familyId
) {}
