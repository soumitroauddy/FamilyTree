package com.familytree.usermgmt.repository;

import com.familytree.usermgmt.model.AuthProvider;
import com.familytree.usermgmt.model.User;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
  private final Map<String, User> users = new ConcurrentHashMap<>();

  public Optional<User> findById(String userId) {
    return Optional.ofNullable(users.get(userId));
  }

  public Optional<User> findByProviderAndProviderUserId(
      AuthProvider provider, String providerUserId) {
    return users.values().stream()
        .filter(user -> user.provider() == provider && user.providerUserId().equals(providerUserId))
        .findFirst();
  }

  public User save(User user) {
    users.put(user.id(), user);
    return user;
  }
}
