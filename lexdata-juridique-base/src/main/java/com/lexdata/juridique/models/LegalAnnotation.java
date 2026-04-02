package com.lexdata.juridique.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "legal_annotations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId; // Proprietaire de l'annotation

    @ManyToOne
    @JoinColumn(name = "texte_id", nullable = false)
    private TexteJuridique texte;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String note;

    // ==========================================
    // LA CORRECTION EST ICI 👇
    // ==========================================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Si vous souhaitez aussi garder la trace des modifications :
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
