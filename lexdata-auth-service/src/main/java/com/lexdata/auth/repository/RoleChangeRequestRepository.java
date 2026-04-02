package com.lexdata.auth.repository;

import com.lexdata.auth.models.RoleChangeRequest;
import com.lexdata.auth.models.RoleChangeRequestStatus;
import com.lexdata.auth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleChangeRequestRepository extends JpaRepository<RoleChangeRequest, Long> {
    Optional<RoleChangeRequest> findByUserAndStatus(User user, RoleChangeRequestStatus status);
    List<RoleChangeRequest> findAllByOrderByCreatedAtDesc();
}
