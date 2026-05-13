package com.familytree.usermgmt.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFamilyRequest(@NotBlank(message = "familyName is required") String familyName) {}
