package com.familytree.usermgmt.config;

import com.familytree.usermgmt.service.UserSyncService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class SupabaseJwtFilter extends OncePerRequestFilter {

  private final SecretKey secretKey;
  private final UserSyncService userSyncService;

  /**
   * A secret shorter than 32 bytes means JWT is disabled (local dev mode).
   * In that mode, the filter falls back to the X-Dev-User-Id header so
   * developers can test the API locally without a real Supabase session.
   */
  public SupabaseJwtFilter(String jwtSecret, UserSyncService userSyncService) {
    SecretKey key = null;
    if (jwtSecret != null && jwtSecret.length() >= 32) {
      key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    this.secretKey = key;
    this.userSyncService = userSyncService;
  }

  public boolean isJwtEnabled() {
    return secretKey != null;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    if (secretKey != null) {
      authenticateViaJwt(request);
    } else {
      authenticateViaDevHeader(request);
    }

    filterChain.doFilter(request, response);
  }

  private void authenticateViaJwt(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) return;

    String token = header.substring(7);
    try {
      Claims claims = Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();

      String userId = claims.getSubject();
      if (userId != null) {
        String email = claims.get("email", String.class);
        String displayName = extractDisplayName(claims);
        userSyncService.syncUser(userId, email, displayName);
        setAuthentication(userId);
      }
    } catch (JwtException | IllegalArgumentException e) {
      SecurityContextHolder.clearContext();
    }
  }

  private void authenticateViaDevHeader(HttpServletRequest request) {
    String devUserId = request.getHeader("X-Dev-User-Id");
    if (devUserId != null && !devUserId.isBlank()) {
      setAuthentication(devUserId.trim());
    }
  }

  private void setAuthentication(String userId) {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
  }

  @SuppressWarnings("unchecked")
  private String extractDisplayName(Claims claims) {
    Object meta = claims.get("user_metadata");
    if (meta instanceof Map<?, ?> map) {
      Object dn = map.get("display_name");
      if (dn instanceof String s && !s.isBlank()) return s;
      Object fullName = map.get("full_name");
      if (fullName instanceof String s && !s.isBlank()) return s;
    }
    return null;
  }
}
