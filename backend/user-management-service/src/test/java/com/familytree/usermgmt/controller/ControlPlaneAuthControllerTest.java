package com.familytree.usermgmt.controller;

import com.familytree.usermgmt.dto.SocialAuthRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControlPlaneAuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void socialAuthCreatesUserForGmail() throws Exception {
    SocialAuthRequest request = new SocialAuthRequest(
        "gmail",
        "gmail-subject-1",
        "gmail.user@example.com",
        "Gmail User");

    mockMvc.perform(post("/api/control-plane/v1/auth/social")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("gmail.user@example.com"))
        .andExpect(jsonPath("$.provider").value("GMAIL"))
        .andExpect(jsonPath("$.accessToken").exists());
  }
}
