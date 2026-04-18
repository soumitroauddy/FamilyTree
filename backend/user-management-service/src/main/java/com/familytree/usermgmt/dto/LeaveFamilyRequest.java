package com.familytree.usermgmt.dto;

import jakarta.validation.constraints.NotBlank;

public record LeaveFamilyRequest(
    @NotBlank(message = "familyId is required")
    String familyId
) {}
