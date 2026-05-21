package com.familytree.usermgmt.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves Supabase ES256 JWT signing keys from the project JWKS endpoint.
 * Supabase access tokens use asymmetric ES256 signatures; the legacy JWT secret
 * only applies to HS256 tokens.
 */
public class SupabaseJwksKeyResolver {

  private static final Duration CACHE_TTL = Duration.ofHours(1);

  private final String jwksUrl;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final ConcurrentHashMap<String, Key> keysByKid = new ConcurrentHashMap<>();
  private volatile Instant fetchedAt = Instant.EPOCH;

  public SupabaseJwksKeyResolver(String supabaseUrl) {
    this(supabaseUrl, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(), new ObjectMapper());
  }

  SupabaseJwksKeyResolver(String supabaseUrl, HttpClient httpClient, ObjectMapper objectMapper) {
    if (supabaseUrl == null || supabaseUrl.isBlank()) {
      this.jwksUrl = null;
    } else {
      this.jwksUrl = supabaseUrl.replaceAll("/+$", "") + "/auth/v1/.well-known/jwks.json";
    }
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
  }

  public boolean isConfigured() {
    return jwksUrl != null;
  }

  public Key resolve(String kid) {
    if (!isConfigured()) {
      throw new IllegalStateException("Supabase JWKS URL is not configured");
    }
    if (kid == null || kid.isBlank()) {
      throw new IllegalArgumentException("JWT kid header is required for ES256 validation");
    }

    refreshIfStale(false);
    Key key = keysByKid.get(kid);
    if (key != null) {
      return key;
    }

    refreshIfStale(true);
    key = keysByKid.get(kid);
    if (key == null) {
      throw new IllegalArgumentException("Unknown JWT key id: " + kid);
    }
    return key;
  }

  static ECPublicKey toEcPublicKey(String xCoord, String yCoord) {
    try {
      BigInteger x = new BigInteger(1, Base64.getUrlDecoder().decode(xCoord));
      BigInteger y = new BigInteger(1, Base64.getUrlDecoder().decode(yCoord));

      AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
      params.init(new ECGenParameterSpec("secp256r1"));
      ECParameterSpec ecSpec = params.getParameterSpec(ECParameterSpec.class);

      KeyFactory keyFactory = KeyFactory.getInstance("EC");
      return (ECPublicKey) keyFactory.generatePublic(new ECPublicKeySpec(new ECPoint(x, y), ecSpec));
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to build EC public key from JWKS coordinates", ex);
    }
  }

  Map<String, Key> parseJwks(String jwksJson) {
    try {
      JsonNode keys = objectMapper.readTree(jwksJson).path("keys");
      Map<String, Key> parsed = new HashMap<>();
      if (!keys.isArray()) {
        return parsed;
      }

      for (JsonNode jwk : keys) {
        if (!"EC".equals(jwk.path("kty").asText())) {
          continue;
        }
        if (!"P-256".equals(jwk.path("crv").asText())) {
          continue;
        }
        String kid = jwk.path("kid").asText(null);
        String x = jwk.path("x").asText(null);
        String y = jwk.path("y").asText(null);
        if (kid == null || x == null || y == null) {
          continue;
        }
        parsed.put(kid, toEcPublicKey(x, y));
      }
      return parsed;
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to parse Supabase JWKS document", ex);
    }
  }

  private void refreshIfStale(boolean force) {
    Instant now = Instant.now();
    if (!force && fetchedAt.plus(CACHE_TTL).isAfter(now) && !keysByKid.isEmpty()) {
      return;
    }

    synchronized (this) {
      now = Instant.now();
      if (!force && fetchedAt.plus(CACHE_TTL).isAfter(now) && !keysByKid.isEmpty()) {
        return;
      }

      try {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(jwksUrl))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
          throw new IllegalStateException(
              "Supabase JWKS request failed with status " + response.statusCode());
        }

        Map<String, Key> parsed = parseJwks(response.body());
        keysByKid.clear();
        keysByKid.putAll(parsed);
        fetchedAt = Instant.now();
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while fetching Supabase JWKS", ex);
      } catch (Exception ex) {
        throw new IllegalStateException("Failed to fetch Supabase JWKS from " + jwksUrl, ex);
      }
    }
  }
}
