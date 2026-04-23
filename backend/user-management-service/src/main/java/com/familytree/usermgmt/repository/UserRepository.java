package com.familytree.usermgmt.repository;

import com.familytree.usermgmt.model.AuthProvider;
import com.familytree.usermgmt.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
  User save(User user);

  Optional<User> findById(String userId);

  Optional<User> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

  List<User> findAll();
}
