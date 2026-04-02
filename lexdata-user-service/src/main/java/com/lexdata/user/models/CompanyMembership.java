package com.lexdata.user.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_memberships")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_profile_id", nullable = false)
    private UserProfile companyProfile; // Le profil de type ENTREPRISE

    @Column(nullable = false)
    private String employeeEmail;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MembershipRole role = MembershipRole.UTILISATEUR_ENTREPRISE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MembershipStatus status = MembershipStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime invitedAt;

    private LocalDateTime acceptedAt;

    public enum MembershipRole {
        ADMIN_ENTREPRISE,
        UTILISATEUR_ENTREPRISE
    }

    public enum MembershipStatus {
        PENDING,
        ACCEPTED,
        REVOKED
    }
}
