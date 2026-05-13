package com.familytree.usermgmt.repository;

import com.familytree.usermgmt.model.Family;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryFamilyRepository implements FamilyRepository {
  private final ConcurrentHashMap<String, Family> store = new ConcurrentHashMap<>();

  @Override
  public Family save(Family family) {
    store.put(family.id(), family);
    return family;
  }

  @Override
  public Optional<Family> findById(String id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Optional<Family> findByJoinCode(String joinCode) {
    return store.values().stream()
        .filter(f -> f.joinCode().equalsIgnoreCase(joinCode))
        .findFirst();
  }

  @Override
  public List<Family> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public List<Family> findAllByMemberUserId(String userId) {
    List<Family> result = new ArrayList<>();
    for (Family family : store.values()) {
      if (family.memberUserIds().contains(userId)) {
        result.add(family);
      }
    }
    return result;
  }

  @Override
  public void deleteAll() {
    store.clear();
  }
}
