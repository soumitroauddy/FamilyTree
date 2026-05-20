package com.familytree.usermgmt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "invitations")
public class InvitationEntity {

  @Id
  private String id;

  @Column(name = "family_id", nullable = false)
  private String familyId;

  @Column(name = "inviter_user_id", nullable = false)
  private String inviterUserId;

  @Column(name = "invite_code", unique = true, nullable = false)
  private String inviteCode;

  @Column(name = "invitee_email")
  private String inviteeEmail;

  @Column(nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  protected InvitationEntity() {}

  public InvitationEntity(
      String id,
      String familyId,
      String inviterUserId,
      String inviteCode,
      String inviteeEmail,
      String status,
      Instant createdAt,
      Instant expiresAt) {
    this.id = id;
    this.familyId = familyId;
    this.inviterUserId = inviterUserId;
    this.inviteCode = inviteCode;
    this.inviteeEmail = inviteeEmail;
    this.status = status;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
  }

  public String getId() { return id; }
  public String getFamilyId() { return familyId; }
  public String getInviterUserId() { return inviterUserId; }
  public String getInviteCode() { return inviteCode; }
  public String getInviteeEmail() { return inviteeEmail; }
  public String getStatus() { return status; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getExpiresAt() { return expiresAt; }

  public void setStatus(String status) { this.status = status; }
}
