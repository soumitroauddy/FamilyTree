package com.familytree.usermgmt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class UserEntity {

  @Id
  private String id;

  @Column(unique = true)
  private String username;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(nullable = false)
  private String email;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Column(name = "auth_provider", nullable = false)
  private String authProvider;

  @Column(name = "provider_user_id")
  private String providerUserId;

  @Column(name = "family_id")
  private String familyId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected UserEntity() {}

  public UserEntity(
      String id,
      String username,
      String passwordHash,
      String email,
      String displayName,
      String authProvider,
      String providerUserId,
      String familyId,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.email = email;
    this.displayName = displayName;
    this.authProvider = authProvider;
    this.providerUserId = providerUserId;
    this.familyId = familyId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() { return id; }
  public String getUsername() { return username; }
  public String getPasswordHash() { return passwordHash; }
  public String getEmail() { return email; }
  public String getDisplayName() { return displayName; }
  public String getAuthProvider() { return authProvider; }
  public String getProviderUserId() { return providerUserId; }
  public String getFamilyId() { return familyId; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }

  public void setUsername(String username) { this.username = username; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public void setEmail(String email) { this.email = email; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }
  public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
  public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }
  public void setFamilyId(String familyId) { this.familyId = familyId; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
