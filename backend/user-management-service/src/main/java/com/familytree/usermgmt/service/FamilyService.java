package com.familytree.usermgmt.service;

import com.familytree.usermgmt.dto.CreateFamilyMemberRequest;
import com.familytree.usermgmt.dto.CreateFamilyRequest;
import com.familytree.usermgmt.dto.DataPlaneBootstrapResponse;
import com.familytree.usermgmt.dto.FamilyExportResponse;
import com.familytree.usermgmt.dto.FamilyMemberResponse;
import com.familytree.usermgmt.dto.FamilyResponse;
import com.familytree.usermgmt.dto.JoinFamilyRequest;
import com.familytree.usermgmt.dto.LeaveFamilyRequest;
import com.familytree.usermgmt.dto.UpdateFamilyMemberRequest;
import com.familytree.usermgmt.exception.ForbiddenException;
import com.familytree.usermgmt.exception.NotFoundException;
import com.familytree.usermgmt.exception.ValidationException;
import com.familytree.usermgmt.model.FamilyEntity;
import com.familytree.usermgmt.model.FamilyMemberEntity;
import com.familytree.usermgmt.model.UserEntity;
import com.familytree.usermgmt.repository.JpaFamilyMemberRepository;
import com.familytree.usermgmt.repository.JpaFamilyRepository;
import com.familytree.usermgmt.repository.JpaUserRepository;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyService {

  private final JpaFamilyRepository familyRepository;
  private final JpaUserRepository userRepository;
  private final JpaFamilyMemberRepository memberRepository;
  private final StorageService storageService;

  public FamilyService(
      JpaFamilyRepository familyRepository,
      JpaUserRepository userRepository,
      JpaFamilyMemberRepository memberRepository,
      StorageService storageService) {
    this.familyRepository = familyRepository;
    this.userRepository = userRepository;
    this.memberRepository = memberRepository;
    this.storageService = storageService;
  }

  @Transactional
  public FamilyResponse createFamily(String userId, CreateFamilyRequest request) {
    UserEntity actor = requireUser(userId);
    if (actor.getFamilyId() != null) {
      throw new ValidationException("User already belongs to a family");
    }

    String name = request.familyName().trim();
    String joinCode = generateJoinCode(name);
    Instant now = Instant.now();

    FamilyEntity family = new FamilyEntity(UUID.randomUUID().toString(), name, joinCode, actor.getId(), now);
    familyRepository.save(family);

    actor.setFamilyId(family.getId());
    actor.setUpdatedAt(now);
    userRepository.save(actor);

    return toFamilyResponse(family);
  }

  @Transactional
  public FamilyResponse joinFamily(String userId, JoinFamilyRequest request) {
    UserEntity actor = requireUser(userId);
    if (actor.getFamilyId() != null) {
      throw new ValidationException("User already belongs to a family");
    }

    FamilyEntity family = familyRepository
        .findByJoinCode(request.joinCode().trim().toUpperCase())
        .orElseThrow(() -> new NotFoundException("Family join code not found"));

    actor.setFamilyId(family.getId());
    actor.setUpdatedAt(Instant.now());
    userRepository.save(actor);

    return toFamilyResponse(family);
  }

  @Transactional
  public FamilyResponse leaveFamily(String userId, LeaveFamilyRequest request) {
    UserEntity actor = requireUser(userId);
    String targetFamilyId =
        (request.familyId() == null || request.familyId().isBlank())
            ? actor.getFamilyId()
            : request.familyId().trim();

    if (targetFamilyId == null) {
      throw new ValidationException("User is not in a family");
    }

    FamilyEntity family = familyRepository
        .findById(targetFamilyId)
        .orElseThrow(() -> new NotFoundException("Family not found"));

    if (!targetFamilyId.equals(actor.getFamilyId())) {
      throw new ValidationException("User is not a member of this family");
    }

    actor.setFamilyId(null);
    actor.setUpdatedAt(Instant.now());
    userRepository.save(actor);

    return toFamilyResponse(family);
  }

  @Transactional(readOnly = true)
  public DataPlaneBootstrapResponse bootstrap(String userId) {
    UserEntity user = requireUser(userId);
    String familyId = user.getFamilyId();
    String familyName = null;
    if (familyId != null) {
      FamilyEntity family = familyRepository
          .findById(familyId)
          .orElseThrow(() -> new NotFoundException("Family not found for user"));
      familyName = family.getName();
    }
    return new DataPlaneBootstrapResponse(
        user.getId(), user.getDisplayName(), user.getEmail(),
        user.getAuthProvider(), familyId, familyName);
  }

  @Transactional
  public FamilyMemberResponse addMember(String userId, String familyId, CreateFamilyMemberRequest request) {
    requireFamilyMembership(userId, familyId);
    Instant now = Instant.now();

    FamilyMemberEntity member = new FamilyMemberEntity(
        UUID.randomUUID().toString(),
        familyId,
        userId,
        request.fullName().trim(),
        null,
        request.birthYear(),
        request.deathYear(),
        request.bio(),
        request.location(),
        request.parentMemberId(),
        now,
        now);

    memberRepository.save(member);
    return toMemberResponse(member);
  }

  @Transactional(readOnly = true)
  public List<FamilyMemberResponse> listMembers(String userId, String familyId) {
    requireFamilyMembership(userId, familyId);
    return memberRepository.findByFamilyId(familyId)
        .stream()
        .map(this::toMemberResponse)
        .toList();
  }

  @Transactional
  public FamilyMemberResponse updateMember(
      String userId, String familyId, String memberId, UpdateFamilyMemberRequest request) {
    requireFamilyMembership(userId, familyId);

    FamilyMemberEntity member = memberRepository.findById(memberId)
        .orElseThrow(() -> new NotFoundException("Member not found"));

    if (!familyId.equals(member.getFamilyId())) {
      throw new ForbiddenException("Member does not belong to this family");
    }

    if (request.fullName() != null) member.setFullName(request.fullName().trim());
    if (request.birthYear() != null) member.setBirthYear(request.birthYear());
    if (request.deathYear() != null) member.setDeathYear(request.deathYear());
    if (request.bio() != null) member.setBio(request.bio());
    if (request.location() != null) member.setLocation(request.location());
    if (request.parentMemberId() != null) member.setParentMemberId(request.parentMemberId());
    member.setUpdatedAt(Instant.now());

    memberRepository.save(member);
    return toMemberResponse(member);
  }

  @Transactional
  public FamilyMemberResponse uploadMemberPhoto(
      String userId, String familyId, String memberId,
      InputStream inputStream, String contentType, long contentLength) {
    requireFamilyMembership(userId, familyId);

    FamilyMemberEntity member = memberRepository.findById(memberId)
        .orElseThrow(() -> new NotFoundException("Member not found"));

    if (!familyId.equals(member.getFamilyId())) {
      throw new ForbiddenException("Member does not belong to this family");
    }

    String photoKey = storageService.uploadPhoto(familyId, memberId, inputStream, contentType, contentLength);
    member.setPhotoUrl(photoKey);
    member.setUpdatedAt(Instant.now());
    memberRepository.save(member);

    return toMemberResponse(member);
  }

  @Transactional(readOnly = true)
  public FamilyExportResponse exportTree(String userId, String familyId) {
    requireFamilyMembership(userId, familyId);

    FamilyEntity family = familyRepository.findById(familyId)
        .orElseThrow(() -> new NotFoundException("Family not found"));

    List<FamilyMemberResponse> members = memberRepository.findByFamilyId(familyId)
        .stream()
        .map(this::toMemberResponse)
        .toList();

    return new FamilyExportResponse(family.getId(), family.getName(), Instant.now(), members);
  }

  private FamilyMemberResponse toMemberResponse(FamilyMemberEntity member) {
    return FamilyMemberResponse.from(member, storageService.resolvePhotoAccessUrl(member.getPhotoUrl()));
  }

  public void requireFamilyMembership(String userId, String familyId) {
    UserEntity user = requireUser(userId);
    if (!familyId.equals(user.getFamilyId())) {
      throw new ForbiddenException("You are not a member of this family");
    }
  }

  private UserEntity requireUser(String userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }

  private FamilyResponse toFamilyResponse(FamilyEntity family) {
    List<UserEntity> members = userRepository.findByFamilyId(family.getId());
    Set<String> memberIds = members.stream()
        .map(UserEntity::getId)
        .collect(Collectors.toSet());
    return new FamilyResponse(
        family.getId(), family.getName(), family.getJoinCode(),
        family.getOwnerUserId(), memberIds, family.getCreatedAt());
  }

  private String generateJoinCode(String familyName) {
    String base = familyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    String prefix = base.length() >= 3 ? base.substring(0, 3) : (base + "XXX").substring(0, 3);
    String suffix = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    return prefix + "-" + suffix;
  }
}
