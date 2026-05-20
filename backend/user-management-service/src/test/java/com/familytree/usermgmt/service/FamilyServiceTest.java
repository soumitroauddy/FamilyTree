package com.familytree.usermgmt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.familytree.usermgmt.dto.CreateFamilyRequest;
import com.familytree.usermgmt.dto.FamilyResponse;
import com.familytree.usermgmt.dto.JoinFamilyRequest;
import com.familytree.usermgmt.dto.LeaveFamilyRequest;
import com.familytree.usermgmt.exception.NotFoundException;
import com.familytree.usermgmt.model.FamilyEntity;
import com.familytree.usermgmt.model.UserEntity;
import com.familytree.usermgmt.repository.JpaFamilyMemberRepository;
import com.familytree.usermgmt.repository.JpaFamilyRepository;
import com.familytree.usermgmt.repository.JpaUserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FamilyServiceTest {

  @Mock private JpaFamilyRepository familyRepository;
  @Mock private JpaUserRepository userRepository;
  @Mock private JpaFamilyMemberRepository memberRepository;
  @Mock private StorageService storageService;

  private FamilyService familyService;

  @BeforeEach
  void setUp() {
    familyService = new FamilyService(familyRepository, userRepository, memberRepository, storageService);
  }

  private UserEntity makeUser(String id, String familyId) {
    return new UserEntity(id, null, null, id + "@test.com", "User " + id,
        "SUPABASE", id, familyId, Instant.now(), Instant.now());
  }

  @Test
  void createFamilyAssignsOwnerAndGeneratesJoinCode() {
    UserEntity owner = makeUser("owner-1", null);
    when(userRepository.findById("owner-1")).thenReturn(Optional.of(owner));
    when(familyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(userRepository.findByFamilyId(any())).thenReturn(List.of(owner));

    FamilyResponse result = familyService.createFamily("owner-1", new CreateFamilyRequest("Stone Family"));

    assertEquals("owner-1", result.ownerUserId());
    assertEquals("Stone Family", result.name());
    assertTrue(result.joinCode().startsWith("STO-"));
    assertTrue(result.memberUserIds().contains("owner-1"));
  }

  @Test
  void joinFamilyByJoinCodeAddsUser() {
    UserEntity member = makeUser("member-1", null);
    FamilyEntity family = new FamilyEntity("fam-1", "Stone Family", "STO-ABCDE", "owner-1", Instant.now());

    when(userRepository.findById("member-1")).thenReturn(Optional.of(member));
    when(familyRepository.findByJoinCode("STO-ABCDE")).thenReturn(Optional.of(family));
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(userRepository.findByFamilyId("fam-1")).thenReturn(List.of(member));

    FamilyResponse result = familyService.joinFamily("member-1", new JoinFamilyRequest("STO-ABCDE"));

    assertEquals("fam-1", result.id());
    assertTrue(result.memberUserIds().contains("member-1"));
  }

  @Test
  void leaveFamilyRemovesUser() {
    UserEntity member = makeUser("member-1", "fam-1");
    FamilyEntity family = new FamilyEntity("fam-1", "Stone Family", "STO-ABCDE", "owner-1", Instant.now());

    when(userRepository.findById("member-1")).thenReturn(Optional.of(member));
    when(familyRepository.findById("fam-1")).thenReturn(Optional.of(family));
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(userRepository.findByFamilyId("fam-1")).thenReturn(List.of());

    FamilyResponse result = familyService.leaveFamily("member-1", new LeaveFamilyRequest("fam-1"));

    assertEquals("fam-1", result.id());
    assertFalse(result.memberUserIds().contains("member-1"));
  }

  @Test
  void joinWithInvalidCodeThrows() {
    UserEntity user = makeUser("user-1", null);
    when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
    when(familyRepository.findByJoinCode("BAD-CODE")).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> familyService.joinFamily("user-1", new JoinFamilyRequest("BAD-CODE")));
  }
}
