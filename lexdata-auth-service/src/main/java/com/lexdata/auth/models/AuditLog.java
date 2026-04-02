package com.lexdata.auth.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Log d'audit traçant chaque tentative de connexion (réussie ou échouée).
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_log_username", columnList = "username"),
        @Index(name = "idx_audit_log_ip", columnList = "ip_address"),
        @Index(name = "idx_audit_log_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username; // Le login saisi (peut ne pas exister en base)

    @Column(name = "user_id")
    private Long userId; // L'ID réel de l'utilisateur (si le compte existe)

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoginStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
