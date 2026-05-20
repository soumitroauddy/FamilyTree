package com.familytree.usermgmt.service;

import com.familytree.usermgmt.dto.CreateInvitationRequest;
import com.familytree.usermgmt.dto.InvitationResponse;
import com.familytree.usermgmt.exception.ForbiddenException;
import com.familytree.usermgmt.exception.NotFoundException;
import com.familytree.usermgmt.exception.ValidationException;
import com.familytree.usermgmt.model.FamilyEntity;
import com.familytree.usermgmt.model.InvitationEntity;
import com.familytree.usermgmt.model.UserEntity;
import com.familytree.usermgmt.repository.JpaFamilyRepository;
import com.familytree.usermgmt.repository.JpaInvitationRepository;
import com.familytree.usermgmt.repository.JpaUserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvitationService {

  private final JpaInvitationRepository invitationRepository;
  private final JpaUserRepository userRepository;
  private final JpaFamilyRepository familyRepository;

  public InvitationService(
      JpaInvitationRepository invitationRepository,
      JpaUserRepository userRepository,
      JpaFamilyRepository familyRepository) {
    this.invitationRepository = invitationRepository;
    this.userRepository = userRepository;
    this.familyRepository = familyRepository;
  }

  @Transactional
  public InvitationResponse createInvitation(String userId, CreateInvitationRequest request) {
    UserEntity actor = requireUser(userId);
    if (actor.getFamilyId() == null) {
      throw new ValidationException("You must belong to a family to invite members");
    }

    FamilyEntity family = familyRepository.findById(actor.getFamilyId())
        .orElseThrow(() -> new NotFoundException("Family not found"));

    Instant now = Instant.now();
    String inviteCode = generateInviteCode();

    InvitationEntity invitation = new InvitationEntity(
        UUID.randomUUID().toString(),
        family.getId(),
        userId,
        inviteCode,
        request.email(),
        "PENDING",
        now,
        now.plus(7, ChronoUnit.DAYS));

    invitationRepository.save(invitation);

    return toResponse(invitation, family, actor);
  }

  @Transactional(readOnly = true)
  public InvitationResponse getInvitationDetails(String code) {
    InvitationEntity invitation = invitationRepository.findByInviteCode(code)
        .orElseThrow(() -> new NotFoundException("Invitation not found"));

    FamilyEntity family = familyRepository.findById(invitation.getFamilyId())
        .orElseThrow(() -> new NotFoundException("Family not found"));

    UserEntity inviter = requireUser(invitation.getInviterUserId());

    return toResponse(invitation, family, inviter);
  }

  @Transactional
  public InvitationResponse acceptInvitation(String userId, String code) {
    UserEntity actor = requireUser(userId);

    InvitationEntity invitation = invitationRepository.findByInviteCode(code)
        .orElseThrow(() -> new NotFoundException("Invitation not found"));

    if (!"PENDING".equals(invitation.getStatus())) {
      throw new ValidationException("Invitation is no longer valid");
    }

    if (Instant.now().isAfter(invitation.getExpiresAt())) {
      invitation.setStatus("EXPIRED");
      invitationRepository.save(invitation);
      throw new ValidationException("Invitation has expired");
    }

    if (actor.getFamilyId() != null) {
      throw new ValidationException("You already belong to a family");
    }

    FamilyEntity family = familyRepository.findById(invitation.getFamilyId())
        .orElseThrow(() -> new NotFoundException("Family not found"));

    actor.setFamilyId(family.getId());
    actor.setUpdatedAt(Instant.now());
    userRepository.save(actor);

    invitation.setStatus("ACCEPTED");
    invitationRepository.save(invitation);

    return toResponse(invitation, family, requireUser(invitation.getInviterUserId()));
  }

  private UserEntity requireUser(String userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }

  private InvitationResponse toResponse(InvitationEntity inv, FamilyEntity family, UserEntity inviter) {
    return new InvitationResponse(
        inv.getId(),
        inv.getInviteCode(),
        family.getId(),
        family.getName(),
        inviter.getDisplayName(),
        inv.getInviteeEmail(),
        inv.getStatus(),
        inv.getCreatedAt(),
        inv.getExpiresAt());
  }

  private String generateInviteCode() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
  }
}
