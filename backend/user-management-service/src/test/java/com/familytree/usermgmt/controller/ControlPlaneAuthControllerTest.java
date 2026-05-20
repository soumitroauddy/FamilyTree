package com.familytree.usermgmt.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.familytree.usermgmt.config.SecurityConfig;
import com.familytree.usermgmt.config.SupabaseProperties;
import com.familytree.usermgmt.controller.controlplane.ControlPlaneAuthController;
import com.familytree.usermgmt.dto.DataPlaneBootstrapResponse;
import com.familytree.usermgmt.service.FamilyService;
import com.familytree.usermgmt.service.UserSyncService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ControlPlaneAuthController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties(SupabaseProperties.class)
@TestPropertySource(properties = "app.supabase.jwt-secret=dev-local")
class ControlPlaneAuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private FamilyService familyService;
  @MockBean private UserSyncService userSyncService;

  @Test
  void syncReturnsBootstrapPayload() throws Exception {
    DataPlaneBootstrapResponse bootstrap = new DataPlaneBootstrapResponse(
        "supabase-user-uuid-1", "Alice", "alice@example.com", "SUPABASE", null, null);
    Mockito.when(familyService.bootstrap("supabase-user-uuid-1")).thenReturn(bootstrap);

    mockMvc.perform(
            post("/api/control-plane/v1/auth/sync")
                .header("X-Dev-User-Id", "supabase-user-uuid-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value("supabase-user-uuid-1"))
        .andExpect(jsonPath("$.displayName").value("Alice"));
  }
}
