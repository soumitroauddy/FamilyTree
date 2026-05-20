package com.familytree.usermgmt.repository;

import com.familytree.usermgmt.model.FamilyEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFamilyRepository extends JpaRepository<FamilyEntity, String> {

  Optional<FamilyEntity> findByJoinCode(String joinCode);
}
