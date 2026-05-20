package com.familytree.usermgmt.repository;

import com.familytree.usermgmt.model.InvitationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaInvitationRepository extends JpaRepository<InvitationEntity, String> {

  Optional<InvitationEntity> findByInviteCode(String inviteCode);
}
