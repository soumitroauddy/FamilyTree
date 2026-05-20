package com.familytree.usermgmt.repository;

import com.familytree.usermgmt.model.FamilyMemberEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFamilyMemberRepository extends JpaRepository<FamilyMemberEntity, String> {

  List<FamilyMemberEntity> findByFamilyId(String familyId);

  void deleteByFamilyId(String familyId);

  List<FamilyMemberEntity> findByAddedByUserId(String addedByUserId);
}
