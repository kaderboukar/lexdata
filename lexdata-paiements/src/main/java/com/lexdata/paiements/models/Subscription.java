package com.lexdata.paiements.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    private SubscriptionTier tier = SubscriptionTier.FREE;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private boolean autoRenew = true;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    public enum SubscriptionTier {
        FREE, STANDARD, PROFESSIONNEL, PREMIUM
    }

    public enum SubscriptionStatus {
        ACTIVE, EXPIRED, CANCELLED, PENDING
    }
}
