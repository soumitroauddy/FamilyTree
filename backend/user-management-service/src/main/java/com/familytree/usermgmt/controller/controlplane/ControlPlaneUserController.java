package com.familytree.usermgmt.controller.controlplane;

import com.familytree.usermgmt.dto.ControlPlaneUserResponse;
import com.familytree.usermgmt.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control-plane/v1/users")
public class ControlPlaneUserController {

  private final UserService userService;

  public ControlPlaneUserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public ControlPlaneUserResponse me(@RequestHeader("X-User-Id") String userId) {
    return userService.profile(userId);
  }
}
