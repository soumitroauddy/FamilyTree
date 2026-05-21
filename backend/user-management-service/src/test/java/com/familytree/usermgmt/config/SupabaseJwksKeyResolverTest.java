package com.familytree.usermgmt.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Key;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SupabaseJwksKeyResolverTest {

  private static final String JWKS_JSON =
      """
      {
        "keys": [
          {
            "alg": "ES256",
            "crv": "P-256",
            "ext": true,
            "key_ops": ["verify"],
            "kid": "6c24e6ee-3e67-4020-b228-e5b1366a4ae",
            "kty": "EC",
            "use": "sig",
            "x": "ObhuVHiqFvfGKadzxbO4aWXZ-1cQRMOiM9uwQA_G4Gc",
            "y": "Pp8RC57cO92zGvF3SsAyVD3xFLkHC6N9tTJG4r_EX2g"
          }
        ]
      }
      """;

  @Test
  void parseJwksBuildsEcPublicKeyByKid() {
    SupabaseJwksKeyResolver resolver =
        new SupabaseJwksKeyResolver("https://example.supabase.co", null, new ObjectMapper());

    Map<String, Key> keys = resolver.parseJwks(JWKS_JSON);

    assertThat(keys).containsOnlyKeys("6c24e6ee-3e67-4020-b228-e5b1366a4ae");
    assertThat(keys.get("6c24e6ee-3e67-4020-b228-e5b1366a4ae").getAlgorithm()).isEqualTo("EC");
  }

  @Test
  void isConfiguredWhenSupabaseUrlProvided() {
    SupabaseJwksKeyResolver resolver = new SupabaseJwksKeyResolver("https://example.supabase.co");

    assertThat(resolver.isConfigured()).isTrue();
  }
}
