package com.lexdata.user.repository;

import com.lexdata.user.models.SecurityAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityAuditEventRepository extends JpaRepository<SecurityAuditEvent, Long> {
}
