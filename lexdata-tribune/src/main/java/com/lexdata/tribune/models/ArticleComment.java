package com.lexdata.tribune.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @NotBlank
    private String username;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Builder.Default
    private boolean modere = false;

    @CreationTimestamp
    private LocalDateTime dateCreation;
}
