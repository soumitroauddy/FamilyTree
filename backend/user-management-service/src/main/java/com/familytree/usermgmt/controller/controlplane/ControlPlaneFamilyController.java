package com.familytree.usermgmt.controller.controlplane;

import com.familytree.usermgmt.dto.CreateFamilyRequest;
import com.familytree.usermgmt.dto.FamilyResponse;
import com.familytree.usermgmt.dto.JoinFamilyRequest;
import com.familytree.usermgmt.dto.LeaveFamilyRequest;
import com.familytree.usermgmt.service.FamilyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control-plane/v1/families")
public class ControlPlaneFamilyController {

  private final FamilyService familyService;

  public ControlPlaneFamilyController(FamilyService familyService) {
    this.familyService = familyService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public FamilyResponse createFamily(
      @AuthenticationPrincipal String userId,
      @Valid @RequestBody CreateFamilyRequest request) {
    return familyService.createFamily(userId, request);
  }

  @PostMapping("/{familyId}/join")
  public FamilyResponse joinFamily(
      @AuthenticationPrincipal String userId,
      @PathVariable String familyId,
      @Valid @RequestBody JoinFamilyRequest request) {
    return familyService.joinFamily(userId, request);
  }

  @DeleteMapping("/{familyId}/members/me")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void leaveFamily(
      @AuthenticationPrincipal String userId,
      @PathVariable String familyId) {
    familyService.leaveFamily(userId, new LeaveFamilyRequest(familyId));
  }
}
