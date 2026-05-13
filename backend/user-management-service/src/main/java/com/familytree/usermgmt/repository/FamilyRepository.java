package com.familytree.usermgmt.repository;

import com.familytree.usermgmt.model.Family;
import java.util.List;
import java.util.Optional;

public interface FamilyRepository {
  Family save(Family family);

  Optional<Family> findById(String familyId);

  Optional<Family> findByJoinCode(String joinCode);

  List<Family> findAll();

  List<Family> findAllByMemberUserId(String userId);

  void deleteAll();
}
