package com.familytree.usermgmt.repository;

import com.familytree.usermgmt.model.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, String> {

  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByAuthProviderAndProviderUserId(String authProvider, String providerUserId);

  List<UserEntity> findByFamilyId(String familyId);
}
