package com.lexdata.auth.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_audit_events", indexes = {
        @Index(name = "idx_security_audit_user", columnList = "user_id"),
        @Index(name = "idx_security_audit_actor", columnList = "performed_by"),
        @Index(name = "idx_security_audit_action", columnList = "action")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "performed_by", nullable = false, length = 120)
    private String performedBy;

    @Column(name = "details", length = 500)
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
