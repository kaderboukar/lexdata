package com.lexdata.veille.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "veille_subscriptions", indexes = {
        @Index(name = "idx_subscription_user", columnList = "user_id"),
        @Index(name = "idx_subscription_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeilleSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 120)
    private String userId;

    @ElementCollection
    @CollectionTable(name = "subscription_domaines", joinColumns = @JoinColumn(name = "subscription_id"))
    @Column(name = "domaine", nullable = false, length = 80)
    @Builder.Default
    private Set<String> domaines = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "subscription_types", joinColumns = @JoinColumn(name = "subscription_id"))
    @Column(name = "text_type", nullable = false, length = 80)
    @Builder.Default
    private Set<String> textTypes = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
