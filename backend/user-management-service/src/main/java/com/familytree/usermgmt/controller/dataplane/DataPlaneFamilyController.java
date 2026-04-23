package com.familytree.usermgmt.controller.dataplane;

import com.familytree.usermgmt.dto.DataPlaneBootstrapResponse;
import com.familytree.usermgmt.service.FamilyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dataplane/v1/families")
public class DataPlaneFamilyController {
  private final FamilyService familyService;

  public DataPlaneFamilyController(FamilyService familyService) {
    this.familyService = familyService;
  }

  @GetMapping("/bootstrap")
  public DataPlaneBootstrapResponse bootstrap(@RequestHeader("X-User-Id") String userId) {
    return familyService.bootstrap(userId);
  }
}
