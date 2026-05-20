package com.familytree.usermgmt.config;

import com.familytree.usermgmt.service.UserSyncService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  private final SupabaseProperties supabaseProperties;
  private final UserSyncService userSyncService;

  public SecurityConfig(SupabaseProperties supabaseProperties, UserSyncService userSyncService) {
    this.supabaseProperties = supabaseProperties;
    this.userSyncService = userSyncService;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    SupabaseJwtFilter jwtFilter =
        new SupabaseJwtFilter(supabaseProperties.getJwtSecret(), userSyncService);

    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> {
          auth.requestMatchers(
              "/actuator/health",
              "/actuator/info",
              "/api/control-plane/v1/auth/**",
              "/api/control-plane/v1/invitations/*/details")
              .permitAll();
          if (jwtFilter.isJwtEnabled()) {
            auth.anyRequest().authenticated();
          } else {
            // Local dev mode: JWT secret not configured; trust X-Dev-User-Id header
            auth.anyRequest().permitAll();
          }
        });

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
