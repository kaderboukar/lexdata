package com.lexdata.auth.repository;

import com.lexdata.auth.models.SecurityAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityAuditEventRepository extends JpaRepository<SecurityAuditEvent, Long> {
}
