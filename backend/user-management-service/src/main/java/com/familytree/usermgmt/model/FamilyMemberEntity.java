package com.familytree.usermgmt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "family_members")
public class FamilyMemberEntity {

  @Id
  private String id;

  @Column(name = "family_id", nullable = false)
  private String familyId;

  @Column(name = "added_by_user_id", nullable = false)
  private String addedByUserId;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "photo_url")
  private String photoUrl;

  @Column(name = "birth_year")
  private Integer birthYear;

  @Column(name = "death_year")
  private Integer deathYear;

  @Column
  private String bio;

  @Column
  private String location;

  @Column(name = "parent_member_id")
  private String parentMemberId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected FamilyMemberEntity() {}

  public FamilyMemberEntity(
      String id,
      String familyId,
      String addedByUserId,
      String fullName,
      String photoUrl,
      Integer birthYear,
      Integer deathYear,
      String bio,
      String location,
      String parentMemberId,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.familyId = familyId;
    this.addedByUserId = addedByUserId;
    this.fullName = fullName;
    this.photoUrl = photoUrl;
    this.birthYear = birthYear;
    this.deathYear = deathYear;
    this.bio = bio;
    this.location = location;
    this.parentMemberId = parentMemberId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() { return id; }
  public String getFamilyId() { return familyId; }
  public String getAddedByUserId() { return addedByUserId; }
  public String getFullName() { return fullName; }
  public String getPhotoUrl() { return photoUrl; }
  public Integer getBirthYear() { return birthYear; }
  public Integer getDeathYear() { return deathYear; }
  public String getBio() { return bio; }
  public String getLocation() { return location; }
  public String getParentMemberId() { return parentMemberId; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }

  public void setFullName(String fullName) { this.fullName = fullName; }
  public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
  public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }
  public void setDeathYear(Integer deathYear) { this.deathYear = deathYear; }
  public void setBio(String bio) { this.bio = bio; }
  public void setLocation(String location) { this.location = location; }
  public void setParentMemberId(String parentMemberId) { this.parentMemberId = parentMemberId; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
