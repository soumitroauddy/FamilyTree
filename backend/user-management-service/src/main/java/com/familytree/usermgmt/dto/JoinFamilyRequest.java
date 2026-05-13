package com.familytree.usermgmt.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinFamilyRequest(
    @NotBlank(message = "joinCode is required")
    String joinCode
) {}
