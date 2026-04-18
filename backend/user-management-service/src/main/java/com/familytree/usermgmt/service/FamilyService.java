package com.familytree.usermgmt.service;

import com.familytree.usermgmt.dto.CreateFamilyRequest;
import com.familytree.usermgmt.dto.DataPlaneBootstrapResponse;
import com.familytree.usermgmt.dto.FamilyResponse;
import com.familytree.usermgmt.dto.JoinFamilyRequest;
import com.familytree.usermgmt.dto.LeaveFamilyRequest;
import com.familytree.usermgmt.exception.NotFoundException;
import com.familytree.usermgmt.exception.ValidationException;
import com.familytree.usermgmt.model.Family;
import com.familytree.usermgmt.model.User;
import com.familytree.usermgmt.repository.FamilyRepository;
import com.familytree.usermgmt.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class FamilyService {
  private final FamilyRepository familyRepository;
  private final UserRepository userRepository;

  public FamilyService(FamilyRepository familyRepository, UserRepository userRepository) {
    this.familyRepository = familyRepository;
    this.userRepository = userRepository;
  }

  public FamilyResponse createFamily(String userId, CreateFamilyRequest request) {
    User actor = requireUser(userId);
    if (actor.familyId() != null) {
      throw new ValidationException("User already belongs to a family");
    }

    String name = request.familyName().trim();
    String joinCode = generateJoinCode(name);
    Set<String> members = new LinkedHashSet<>();
    members.add(actor.id());

    Family family =
        new Family(UUID.randomUUID().toString(), name, joinCode, actor.id(), members, Instant.now());
    Family saved = familyRepository.save(family);
    userRepository.save(actor.withFamilyId(saved.id()));
    return FamilyResponse.from(saved);
  }

  public FamilyResponse joinFamily(String userId, JoinFamilyRequest request) {
    User actor = requireUser(userId);
    if (actor.familyId() != null) {
      throw new ValidationException("User already belongs to a family");
    }

    Family family =
        familyRepository
            .findByJoinCode(request.joinCode().trim().toUpperCase())
            .orElseThrow(() -> new NotFoundException("Family join code not found"));

    Set<String> updatedMembers = new LinkedHashSet<>(family.memberUserIds());
    updatedMembers.add(actor.id());
    Family updatedFamily = family.withMemberUserIds(updatedMembers);
    Family saved = familyRepository.save(updatedFamily);
    userRepository.save(actor.withFamilyId(saved.id()));
    return FamilyResponse.from(saved);
  }

  public FamilyResponse leaveFamily(String userId, LeaveFamilyRequest request) {
    User actor = requireUser(userId);
    String targetFamilyId =
        request.familyId() == null || request.familyId().isBlank()
            ? actor.familyId()
            : request.familyId().trim();
    if (targetFamilyId == null) {
      throw new ValidationException("User is not in a family");
    }

    Family family =
        familyRepository
            .findById(targetFamilyId)
            .orElseThrow(() -> new NotFoundException("Family not found"));

    if (!family.memberUserIds().contains(actor.id())) {
      throw new ValidationException("User is not a member of this family");
    }

    Set<String> updatedMembers = new LinkedHashSet<>(family.memberUserIds());
    updatedMembers.remove(actor.id());
    Family updatedFamily = family.withMemberUserIds(updatedMembers);
    Family saved = familyRepository.save(updatedFamily);
    userRepository.save(actor.withFamilyId(null));
    return FamilyResponse.from(saved);
  }

  public DataPlaneBootstrapResponse bootstrap(String userId) {
    User user = requireUser(userId);
    var families = new ArrayList<FamilyResponse>();
    if (user.familyId() != null) {
      Family family =
          familyRepository
              .findById(user.familyId())
              .orElseThrow(() -> new NotFoundException("Family not found for user"));
      families.add(FamilyResponse.from(family));
    }
    return new DataPlaneBootstrapResponse(user.id(), user.email(), user.provider().name(), families);
  }

  private User requireUser(String userId) {
    return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
  }

  private String generateJoinCode(String familyName) {
    String base = familyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    String prefix = base.length() >= 3 ? base.substring(0, 3) : (base + "XXX").substring(0, 3);
    String suffix = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    return prefix + "-" + suffix;
  }
}
