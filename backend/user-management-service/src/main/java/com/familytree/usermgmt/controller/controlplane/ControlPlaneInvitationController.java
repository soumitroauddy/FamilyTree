package com.familytree.usermgmt.controller.controlplane;

import com.familytree.usermgmt.dto.CreateInvitationRequest;
import com.familytree.usermgmt.dto.InvitationResponse;
import com.familytree.usermgmt.service.InvitationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control-plane/v1/invitations")
public class ControlPlaneInvitationController {

  private final InvitationService invitationService;

  public ControlPlaneInvitationController(InvitationService invitationService) {
    this.invitationService = invitationService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public InvitationResponse createInvitation(
      @AuthenticationPrincipal String userId,
      @RequestBody CreateInvitationRequest request) {
    return invitationService.createInvitation(userId, request);
  }

  @GetMapping("/{code}/details")
  public InvitationResponse getInvitationDetails(@PathVariable String code) {
    return invitationService.getInvitationDetails(code);
  }

  @PostMapping("/{code}/accept")
  public InvitationResponse acceptInvitation(
      @AuthenticationPrincipal String userId,
      @PathVariable String code) {
    return invitationService.acceptInvitation(userId, code);
  }
}
