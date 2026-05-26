package com.familytree.usermgmt.config;

import com.familytree.usermgmt.service.UserSyncService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class SupabaseJwtFilter extends OncePerRequestFilter {

  private final boolean productionAuthMode;
  private final SecretKey hmacKey;
  private final SupabaseJwksKeyResolver jwksResolver;
  private final String expectedIssuer;
  private final UserSyncService userSyncService;

  /**
   * A JWT secret shorter than 32 bytes means auth is in local dev mode.
   * In that mode, the filter falls back to the X-Dev-User-Id header so developers can test
   * the API locally without a real Supabase session.
   */
  public SupabaseJwtFilter(String jwtSecret, String supabaseUrl, UserSyncService userSyncService) {
    this.productionAuthMode = jwtSecret != null && jwtSecret.length() >= 32;

    SecretKey key = null;
    if (productionAuthMode) {
      key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    this.hmacKey = key;
    this.jwksResolver =
        productionAuthMode && supabaseUrl != null && !supabaseUrl.isBlank()
            ? new SupabaseJwksKeyResolver(supabaseUrl)
            : null;
    this.expectedIssuer =
        productionAuthMode && supabaseUrl != null && !supabaseUrl.isBlank()
            ? supabaseUrl.replaceAll("/+$", "") + "/auth/v1"
            : null;
    this.userSyncService = userSyncService;
  }

  public boolean isJwtEnabled() {
    return productionAuthMode;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    if (isJwtEnabled()) {
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
      var parserBuilder = Jwts.parser().keyLocator(this::locateKey);
      if (expectedIssuer != null) {
        parserBuilder.requireIssuer(expectedIssuer);
      }

      Claims claims = parserBuilder.build().parseSignedClaims(token).getPayload();

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

  private Key locateKey(Header header) {
    String algorithm = header.getAlgorithm();
    if (algorithm == null) {
      throw new JwtException("JWT algorithm header is missing");
    }

    return switch (algorithm) {
      case "HS256", "HS384", "HS512" -> {
        if (hmacKey == null) {
          throw new JwtException("HMAC JWT validation is not configured");
        }
        yield hmacKey;
      }
      case "ES256" -> {
        if (jwksResolver == null || !jwksResolver.isConfigured()) {
          throw new JwtException("Supabase JWKS validation is not configured");
        }
        yield jwksResolver.resolve(header.getKeyId());
      }
      default -> throw new JwtException("Unsupported JWT algorithm: " + algorithm);
    };
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
