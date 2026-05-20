package com.familytree.usermgmt.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFamilyMemberRequest(
    @NotBlank(message = "fullName is required")
    String fullName,

    String parentMemberId,
    Integer birthYear,
    Integer deathYear,
    String bio,
    String location
) {}
