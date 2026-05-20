package com.familytree.usermgmt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "families")
public class FamilyEntity {

  @Id
  private String id;

  @Column(nullable = false)
  private String name;

  @Column(name = "join_code", unique = true, nullable = false)
  private String joinCode;

  @Column(name = "owner_user_id", nullable = false)
  private String ownerUserId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected FamilyEntity() {}

  public FamilyEntity(String id, String name, String joinCode, String ownerUserId, Instant createdAt) {
    this.id = id;
    this.name = name;
    this.joinCode = joinCode;
    this.ownerUserId = ownerUserId;
    this.createdAt = createdAt;
  }

  public String getId() { return id; }
  public String getName() { return name; }
  public String getJoinCode() { return joinCode; }
  public String getOwnerUserId() { return ownerUserId; }
  public Instant getCreatedAt() { return createdAt; }

  public void setName(String name) { this.name = name; }
}
