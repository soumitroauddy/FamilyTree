package com.familytree.usermgmt.controller.controlplane;

import com.familytree.usermgmt.dto.AuthResponse;
import com.familytree.usermgmt.dto.SocialAuthRequest;
import com.familytree.usermgmt.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control-plane/v1/auth")
public class ControlPlaneAuthController {
  private final AuthService authService;

  public ControlPlaneAuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/social")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse socialAuth(@Valid @RequestBody SocialAuthRequest request) {
    return authService.createOrSignIn(request);
  }
}
