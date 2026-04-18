package com.familytree.usermgmt.model;

import java.util.Locale;

public enum AuthProvider {
  GMAIL,
  FACEBOOK,
  MICROSOFT;

  public static AuthProvider fromInput(String value) {
    if (value == null) {
      throw new IllegalArgumentException("provider is required");
    }
    return switch (value.trim().toLowerCase(Locale.ROOT)) {
      case "gmail", "google" -> GMAIL;
      case "facebook" -> FACEBOOK;
      case "microsoft", "ms" -> MICROSOFT;
      default -> throw new IllegalArgumentException("Unsupported provider: " + value);
    };
  }
}
