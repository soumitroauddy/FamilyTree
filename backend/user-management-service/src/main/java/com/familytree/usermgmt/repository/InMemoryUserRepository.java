package com.familytree.usermgmt.repository;

import com.familytree.usermgmt.model.AuthProvider;
import com.familytree.usermgmt.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserRepository implements UserRepository {
  private final ConcurrentMap<String, User> byId = new ConcurrentHashMap<>();

  @Override
  public User save(User user) {
    byId.put(user.id(), user);
    return user;
  }

  @Override
  public Optional<User> findById(String userId) {
    return Optional.ofNullable(byId.get(userId));
  }

  @Override
  public Optional<User> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
    return byId.values().stream()
        .filter(u -> u.provider() == provider)
        .filter(u -> u.providerUserId().equals(providerUserId))
        .findFirst();
  }

  @Override
  public List<User> findAll() {
    return new ArrayList<>(byId.values());
  }
}
