package com.familytree.usermgmt.controller.controlplane;

import com.familytree.usermgmt.dto.DataPlaneBootstrapResponse;
import com.familytree.usermgmt.service.FamilyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth control-plane endpoints.
 *
 * Registration and login are handled directly by Supabase Auth on the client.
 * The /sync endpoint is called by the Flutter app after a successful Supabase
 * sign-in or sign-up to ensure a local UserEntity exists and to retrieve the
 * current user + family state. The JWT filter auto-creates the user row before
 * this method is invoked, so this is effectively a no-op upsert + bootstrap.
 */
@RestController
@RequestMapping("/api/control-plane/v1/auth")
public class ControlPlaneAuthController {

  private final FamilyService familyService;

  public ControlPlaneAuthController(FamilyService familyService) {
    this.familyService = familyService;
  }

  @PostMapping("/sync")
  public DataPlaneBootstrapResponse sync(@AuthenticationPrincipal String userId) {
    return familyService.bootstrap(userId);
  }
}
