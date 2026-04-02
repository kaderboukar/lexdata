package com.lexdata.user.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_profile_id", referencedColumnName = "id")
    private UserProfile userProfile;

    // --- Thématiques de Veille ---
    @ElementCollection(targetClass = LegalDomain.class)
    @CollectionTable(name = "followed_topics", joinColumns = @JoinColumn(name = "preference_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "topic")
    @Builder.Default
    private Set<LegalDomain> followedTopics = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "alert_keywords", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "keyword")
    @Builder.Default
    private Set<String> alertKeywords = new HashSet<>();

    // --- Notifications ---
    @Builder.Default
    private boolean emailEnabled = true;

    @Builder.Default
    private boolean pushEnabled = false;

    @Builder.Default
    private boolean smsEnabled = false;

    @Builder.Default
    private String timezone = "Africa/Niamey";
}
