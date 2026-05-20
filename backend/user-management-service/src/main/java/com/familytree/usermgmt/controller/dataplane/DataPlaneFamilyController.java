package com.familytree.usermgmt.controller.dataplane;

import com.familytree.usermgmt.dto.CreateFamilyMemberRequest;
import com.familytree.usermgmt.dto.DataPlaneBootstrapResponse;
import com.familytree.usermgmt.dto.FamilyExportResponse;
import com.familytree.usermgmt.dto.FamilyMemberResponse;
import com.familytree.usermgmt.dto.UpdateFamilyMemberRequest;
import com.familytree.usermgmt.service.FamilyService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/data-plane/v1/families")
public class DataPlaneFamilyController {

  private final FamilyService familyService;

  public DataPlaneFamilyController(FamilyService familyService) {
    this.familyService = familyService;
  }

  @GetMapping("/bootstrap")
  public DataPlaneBootstrapResponse bootstrap(@AuthenticationPrincipal String userId) {
    return familyService.bootstrap(userId);
  }

  @PostMapping("/{familyId}/members")
  @ResponseStatus(HttpStatus.CREATED)
  public FamilyMemberResponse addMember(
      @AuthenticationPrincipal String userId,
      @PathVariable String familyId,
      @Valid @RequestBody CreateFamilyMemberRequest request) {
    return familyService.addMember(userId, familyId, request);
  }

  @GetMapping("/{familyId}/members")
  public List<FamilyMemberResponse> listMembers(
      @AuthenticationPrincipal String userId,
      @PathVariable String familyId) {
    return familyService.listMembers(userId, familyId);
  }

  @PutMapping("/{familyId}/members/{memberId}")
  public FamilyMemberResponse updateMember(
      @AuthenticationPrincipal String userId,
      @PathVariable String familyId,
      @PathVariable String memberId,
      @RequestBody UpdateFamilyMemberRequest request) {
    return familyService.updateMember(userId, familyId, memberId, request);
  }

  @PostMapping("/{familyId}/members/{memberId}/photo")
  public FamilyMemberResponse uploadPhoto(
      @AuthenticationPrincipal String userId,
      @PathVariable String familyId,
      @PathVariable String memberId,
      @RequestParam("photo") MultipartFile photo) throws IOException {
    return familyService.uploadMemberPhoto(
        userId, familyId, memberId,
        photo.getInputStream(),
        photo.getContentType(),
        photo.getSize());
  }

  @GetMapping("/{familyId}/export")
  public FamilyExportResponse exportTree(
      @AuthenticationPrincipal String userId,
      @PathVariable String familyId) {
    return familyService.exportTree(userId, familyId);
  }
}
