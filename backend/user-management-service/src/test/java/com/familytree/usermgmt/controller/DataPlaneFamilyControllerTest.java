package com.familytree.usermgmt.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.familytree.usermgmt.dto.DataPlaneBootstrapResponse;
import com.familytree.usermgmt.service.FamilyService;
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
            "Mobile User",
            "mobile@app.test",
            "GOOGLE",
            "family-1",
            "Stone Family");
    Mockito.when(familyService.bootstrap("user-1")).thenReturn(response);

    mockMvc
        .perform(get("/api/dataplane/v1/families/bootstrap").header("X-User-Id", "user-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value("user-1"))
        .andExpect(jsonPath("$.familyName").value("Stone Family"));
  }
}
