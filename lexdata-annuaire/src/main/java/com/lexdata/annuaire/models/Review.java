package com.lexdata.annuaire.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfessionalProfile profile;

    @NotBlank
    private String userUsername; // Celui qui laisse l'avis

    @Min(1)
    @Max(5)
    private Integer note;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Builder.Default
    private boolean modere = false;

    @CreationTimestamp
    private LocalDateTime dateCreation;
}
