package com.lexdata.tribune.payload;

import com.lexdata.tribune.models.Article;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class ArticleRequest {
    @NotBlank
    private String titre;
    private String resume;
    @NotBlank
    private String contenuRich;
    private Article.ArticleType type;
    private Set<String> themes;
    private Set<String> keywords;
    private String auteurs;
    private String referencesBibliographiques;
    private String license;
}
