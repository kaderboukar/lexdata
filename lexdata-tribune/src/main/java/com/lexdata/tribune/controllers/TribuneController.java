package com.lexdata.tribune.controllers;

import com.lexdata.tribune.dto.ArticleDto;
import com.lexdata.tribune.models.Article;
import com.lexdata.tribune.models.ArticleComment;
import com.lexdata.tribune.payload.ArticleRequest;
import com.lexdata.tribune.repository.ArticleCommentRepository;
import com.lexdata.tribune.repository.ArticleRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tribune")
public class TribuneController {

    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository commentRepository;

    public TribuneController(ArticleRepository articleRepository, ArticleCommentRepository commentRepository) {
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
    }

    // --- RECHERCHE ET CONSULTATION ---

    @GetMapping("/articles/search")
    public Page<ArticleDto> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) Article.ArticleType type,
            @PageableDefault(size = 10) Pageable pageable) {
        return articleRepository.searchArticles(query, theme, type, pageable).map(this::mapToDto);
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleDto> getArticle(@PathVariable Long id) {
        return articleRepository.findById(id)
                .map(article -> {
                    article.setViewCount(article.getViewCount() + 1);
                    return articleRepository.save(article);
                })
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- SOUMISSION (AUTEURS) ---

    @PostMapping("/articles")
    @PreAuthorize("hasAnyRole('USER', 'JURISTE', 'AVOCAT', 'AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ArticleDto> submitArticle(@Valid @RequestBody ArticleRequest request) {
        Article article = mapToEntity(request);
        article.setStatut(Article.WorkflowStatus.SOUMIS);
        return ResponseEntity.ok(mapToDto(articleRepository.save(article)));
    }

    @PutMapping("/articles/{id}")
    @PreAuthorize("hasAnyRole('USER', 'JURISTE', 'AVOCAT', 'AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ArticleDto> updateMyArticle(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        // En prod, vérifier que l'auteur est bien celui qui modifie
        return articleRepository.findById(id)
                .map(article -> {
                    updateEntityFields(article, request);
                    if (article.getStatut() == Article.WorkflowStatus.REJETE) {
                        article.setStatut(Article.WorkflowStatus.SOUMIS); // Re-soumission suite à correction
                    }
                    return ResponseEntity.ok(mapToDto(articleRepository.save(article)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- FONCTIONS SOCIALES ---

    @PostMapping("/articles/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ArticleDto> likeArticle(@PathVariable Long id) {
        return articleRepository.findById(id)
                .map(article -> {
                    article.setLikeCount(article.getLikeCount() + 1);
                    return ResponseEntity.ok(mapToDto(articleRepository.save(article)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/articles/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> addComment(@PathVariable Long id, @RequestBody String content) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return articleRepository.findById(id)
                .map(article -> {
                    ArticleComment comment = ArticleComment.builder()
                            .article(article)
                            .username(username)
                            .contenu(content)
                            .build();
                    commentRepository.save(comment);
                    return ResponseEntity.ok("Commentaire soumis (en attente de modération).");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/articles/{id}/comments")
    public List<ArticleComment> getComments(@PathVariable Long id) {
        return commentRepository.findByArticleIdAndModereTrueOrderByDateCreationDesc(id);
    }

    // --- CITATIONS ---

    @GetMapping("/articles/{id}/citation")
    public ResponseEntity<String> getCitation(@PathVariable Long id, @RequestParam(defaultValue = "APA") String format) {
        return articleRepository.findById(id)
                .map(article -> {
                    String citation = switch (format.toUpperCase()) {
                        case "CHICAGO" -> String.format("%s, \"%s,\" LexData Tribune (%d).", article.getAuteurs(), article.getTitre(), article.getDateCreation().getYear());
                        default -> String.format("%s (%d). %s. LexData Tribune.", article.getAuteurs(), article.getDateCreation().getYear(), article.getTitre());
                    };
                    return ResponseEntity.ok(citation);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- MODERATION / COMITE SCIENTIFIQUE ---

    @PatchMapping("/admin/articles/{id}/workflow")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ArticleDto> updateWorkflow(@PathVariable Long id, @RequestParam Article.WorkflowStatus status) {
        return articleRepository.findById(id)
                .map(article -> {
                    article.setStatut(status);
                    return ResponseEntity.ok(mapToDto(articleRepository.save(article)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Article mapToEntity(ArticleRequest request) {
        Article article = new Article();
        updateEntityFields(article, request);
        return article;
    }

    private void updateEntityFields(Article article, ArticleRequest request) {
        article.setTitre(request.getTitre());
        article.setResume(request.getResume());
        article.setContenuRich(request.getContenuRich());
        article.setType(request.getType());
        article.setThemes(request.getThemes());
        article.setKeywords(request.getKeywords());
        article.setAuteurs(request.getAuteurs());
        article.setReferencesBibliographiques(request.getReferencesBibliographiques());
        article.setLicense(request.getLicense() != null ? request.getLicense() : "CC BY-NC-SA");
    }

    private ArticleDto mapToDto(Article article) {
        return ArticleDto.builder()
                .id(article.getId())
                .titre(article.getTitre())
                .resume(article.getResume())
                .contenuRich(article.getContenuRich())
                .type(article.getType())
                .themes(article.getThemes())
                .keywords(article.getKeywords())
                .auteurs(article.getAuteurs())
                .referencesBibliographiques(article.getReferencesBibliographiques())
                .statut(article.getStatut().name())
                .license(article.getLicense())
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .dateCreation(article.getDateCreation())
                .dateModification(article.getDateModification())
                .build();
    }
}
