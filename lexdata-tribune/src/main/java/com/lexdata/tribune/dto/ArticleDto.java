package com.lexdata.tribune.dto;

import com.lexdata.tribune.models.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {
    private Long id;
    private String titre;
    private String resume;
    private String contenuRich;
    private Article.ArticleType type;
    private Set<String> themes;
    private Set<String> keywords;
    private String auteurs;
    private String referencesBibliographiques;
    private String statut;
    private String license;
    private Long viewCount;
    private Long likeCount;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
