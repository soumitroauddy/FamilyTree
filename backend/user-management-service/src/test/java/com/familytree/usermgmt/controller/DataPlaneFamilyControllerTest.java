package com.familytree.usermgmt.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.familytree.usermgmt.dto.DataPlaneBootstrapResponse;
import com.familytree.usermgmt.dto.FamilyResponse;
import com.familytree.usermgmt.service.FamilyService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = com.familytree.usermgmt.controller.dataplane.DataPlaneFamilyController.class)
class DataPlaneFamilyControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private FamilyService familyService;

  @Test
  void bootstrapReturnsMobilePayload() throws Exception {
    DataPlaneBootstrapResponse response =
        new DataPlaneBootstrapResponse(
            "user-1",
            "mobile@app.test",
            "MOBILE_USER",
            List.of(new FamilyResponse("family-1", "Stone Family", "STO-ABCDE", List.of("user-1"))));
    Mockito.when(familyService.bootstrapForUser("user-1")).thenReturn(response);

    mockMvc
        .perform(get("/api/dataplane/v1/families/bootstrap").header("X-User-Id", "user-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value("user-1"))
        .andExpect(jsonPath("$.families[0].familyName").value("Stone Family"));
  }
}
