package com.lexdata.annuaire.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfessionalProfile professional;

    @NotBlank
    private String requesterUsername;

    @NotBlank
    private String objet;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String descriptionBesoin;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeadStatus statut = LeadStatus.ENVOYE;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    public enum LeadStatus {
        ENVOYE, RECU, REPONDU, RDV_FIXE, TERMINE, FERME
    }
}
