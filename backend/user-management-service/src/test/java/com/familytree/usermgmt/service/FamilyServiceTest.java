package com.familytree.usermgmt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.familytree.usermgmt.dto.CreateFamilyRequest;
import com.familytree.usermgmt.dto.JoinFamilyRequest;
import com.familytree.usermgmt.dto.LeaveFamilyRequest;
import com.familytree.usermgmt.dto.SocialAuthRequest;
import com.familytree.usermgmt.exception.NotFoundException;
import com.familytree.usermgmt.model.AuthProvider;
import com.familytree.usermgmt.repository.InMemoryFamilyRepository;
import com.familytree.usermgmt.repository.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FamilyServiceTest {

  private AuthService authService;
  private FamilyService familyService;

  @BeforeEach
  void setUp() {
    var userRepo = new InMemoryUserRepository();
    var familyRepo = new InMemoryFamilyRepository();
    authService = new AuthService(userRepo);
    familyService = new FamilyService(familyRepo, userRepo);
  }

  @Test
  void createJoinLeaveFamilyLifecycleWorks() {
    var owner =
        authService.createOrSignIn(
            new SocialAuthRequest(
                AuthProvider.GMAIL.name(),
                "owner-sub",
                "owner@example.com",
                "Owner"));
    var member =
        authService.createOrSignIn(
            new SocialAuthRequest(
                AuthProvider.MICROSOFT.name(),
                "member-sub",
                "member@example.com",
                "Member"));

    var created = familyService.createFamily(owner.userId(), new CreateFamilyRequest("Stone Family"));
    assertEquals(owner.userId(), created.ownerUserId());
    assertTrue(created.memberUserIds().contains(owner.userId()));

    var joined = familyService.joinFamily(member.userId(), new JoinFamilyRequest(created.joinCode()));
    assertTrue(joined.memberUserIds().contains(owner.userId()));
    assertTrue(joined.memberUserIds().contains(member.userId()));

    var afterLeave = familyService.leaveFamily(member.userId(), new LeaveFamilyRequest(created.id()));
    assertTrue(afterLeave.memberUserIds().contains(owner.userId()));
    assertFalse(afterLeave.memberUserIds().contains(member.userId()));
  }

  @Test
  void joinWithInvalidCodeThrows() {
    var user =
        authService.createOrSignIn(
            new SocialAuthRequest(
                AuthProvider.FACEBOOK.name(),
                "subject-1",
                "person@example.com",
                "Person"));

    assertThrows(
        NotFoundException.class,
        () -> familyService.joinFamily(user.userId(), new JoinFamilyRequest("BAD-CODE")));
  }
}
