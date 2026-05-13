package com.familytree.usermgmt.service;

import com.familytree.usermgmt.dto.ControlPlaneUserResponse;
import com.familytree.usermgmt.exception.NotFoundException;
import com.familytree.usermgmt.model.User;
import com.familytree.usermgmt.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User requireUser(String userId) {
    return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
  }

  public ControlPlaneUserResponse profile(String userId) {
    User user = requireUser(userId);
    return new ControlPlaneUserResponse(
        user.id(), user.email(), user.displayName(), user.provider().name(), user.familyId());
  }
}
