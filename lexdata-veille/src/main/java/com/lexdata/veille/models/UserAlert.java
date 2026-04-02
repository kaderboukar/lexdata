package com.lexdata.veille.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_alerts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_alert", columnNames = {"user_id", "alert_id"})
}, indexes = {
        @Index(name = "idx_user_alert_user", columnList = "user_id"),
        @Index(name = "idx_user_alert_read", columnList = "is_read")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 120)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_id", nullable = false)
    private AlerteVeille alert;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    private LocalDateTime readAt;
    private LocalDateTime deletedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
