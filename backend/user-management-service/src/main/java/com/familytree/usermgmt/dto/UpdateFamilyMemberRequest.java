package com.familytree.usermgmt.dto;

public record UpdateFamilyMemberRequest(
    String fullName,
    Integer birthYear,
    Integer deathYear,
    String bio,
    String location,
    String parentMemberId
) {}
