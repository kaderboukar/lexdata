package com.lexdata.admin.repository;

import com.lexdata.admin.models.AgentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentProfileRepository extends JpaRepository<AgentProfile, Long> {
    Optional<AgentProfile> findByUsername(String username);
}
