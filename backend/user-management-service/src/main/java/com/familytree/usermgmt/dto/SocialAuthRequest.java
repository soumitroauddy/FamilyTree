package com.familytree.usermgmt.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialAuthRequest(
    @NotBlank(message = "provider is required")
    String provider,
    @NotBlank(message = "providerUserId is required")
    String providerUserId,
    @NotBlank(message = "email is required")
    String email,
    @NotBlank(message = "displayName is required")
    String displayName
) {}
