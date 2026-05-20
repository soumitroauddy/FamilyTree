package com.familytree.usermgmt.service;

import com.familytree.usermgmt.dto.ControlPlaneUserResponse;
import com.familytree.usermgmt.exception.NotFoundException;
import com.familytree.usermgmt.model.FamilyMemberEntity;
import com.familytree.usermgmt.model.UserEntity;
import com.familytree.usermgmt.repository.JpaFamilyMemberRepository;
import com.familytree.usermgmt.repository.JpaUserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final JpaUserRepository userRepository;
  private final JpaFamilyMemberRepository memberRepository;
  private final StorageService storageService;

  public UserService(
      JpaUserRepository userRepository,
      JpaFamilyMemberRepository memberRepository,
      StorageService storageService) {
    this.userRepository = userRepository;
    this.memberRepository = memberRepository;
    this.storageService = storageService;
  }

  public UserEntity requireUser(String userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }

  @Transactional(readOnly = true)
  public ControlPlaneUserResponse profile(String userId) {
    UserEntity user = requireUser(userId);
    return new ControlPlaneUserResponse(
        user.getId(), user.getEmail(), user.getDisplayName(),
        user.getAuthProvider(), user.getFamilyId());
  }

  @Transactional
  public void deleteAccount(String userId) {
    UserEntity user = requireUser(userId);

    // Remove S3 photos for family members added by this user
    List<FamilyMemberEntity> membersAddedByUser = memberRepository.findByAddedByUserId(userId);
    for (FamilyMemberEntity member : membersAddedByUser) {
      if (member.getPhotoUrl() != null) {
        tryDeletePhoto(member.getPhotoUrl());
      }
    }

    // Nullify familyId (leave the family)
    user.setFamilyId(null);
    userRepository.save(user);

    // Delete the user record
    userRepository.delete(user);
  }

  private void tryDeletePhoto(String storedPhotoRef) {
    try {
      storageService.deletePhoto(storedPhotoRef);
    } catch (Exception ignored) {
      // Best-effort cleanup; don't fail the delete if S3 is unavailable
    }
  }
}
