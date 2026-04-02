package com.lexdata.tribune.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "articles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String resume;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String contenuRich;

    @Enumerated(EnumType.STRING)
    private ArticleType type;

    @ElementCollection
    @CollectionTable(name = "article_themes", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "theme")
    @Builder.Default
    private Set<String> themes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "article_keywords", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "keyword")
    @Builder.Default
    private Set<String> keywords = new HashSet<>();

    private String auteurs; // Format libre ou JSON simplifié: "Nom Prénom (Institution)"
    
    @Column(columnDefinition = "TEXT")
    private String referencesBibliographiques;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WorkflowStatus statut = WorkflowStatus.BROUILLON;

    private String license = "CC BY-NC-SA";

    private Long viewCount = 0L;
    private Long likeCount = 0L;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    public enum ArticleType {
        ARTICLE_DOCTRINAL, COMMENTAIRE_ARRET, NOTE_JURISPRUDENCE, THESE_EXTRAIT, ETUDE_THEMATIQUE, BILLET_ACTUALITE
    }

    public enum WorkflowStatus {
        BROUILLON, SOUMIS, EN_RELECTURE, ACCEPTE, REJETE, PUBLIE
    }
}
