package com.familytree.usermgmt.controller.controlplane;

import com.familytree.usermgmt.dto.ControlPlaneUserResponse;
import com.familytree.usermgmt.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control-plane/v1/users")
public class ControlPlaneUserController {

  private final UserService userService;

  public ControlPlaneUserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public ControlPlaneUserResponse me(@AuthenticationPrincipal String userId) {
    return userService.profile(userId);
  }

  @DeleteMapping("/me")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAccount(@AuthenticationPrincipal String userId) {
    userService.deleteAccount(userId);
  }
}
