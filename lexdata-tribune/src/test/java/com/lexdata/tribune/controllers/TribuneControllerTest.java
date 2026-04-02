package com.lexdata.tribune.controllers;

import com.lexdata.tribune.models.Article;
import com.lexdata.tribune.repository.ArticleCommentRepository;
import com.lexdata.tribune.repository.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TribuneControllerTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleCommentRepository commentRepository;

    @InjectMocks
    private TribuneController tribuneController;

    @Test
    void searchArticles_ShouldReturnPage() {
        // Arrange
        Page<Article> page = new PageImpl<>(Collections.emptyList());
        when(articleRepository.searchArticles(any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        // Act
        Page<?> result = tribuneController.search(null, null, null, mock(Pageable.class));

        // Assert
        assertNotNull(result);
        verify(articleRepository).searchArticles(any(), any(), any(), any(Pageable.class));
    }

    @Test
    void likeArticle_ShouldIncrementLikes() {
        // Arrange
        Long articleId = 1L;
        Article article = Article.builder()
                .id(articleId)
                .titre("Article Test")
                .type(Article.ArticleType.ARTICLE_DOCTRINAL)
                .statut(Article.WorkflowStatus.PUBLIE)
                .likeCount(10L)
                .build();

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(articleRepository.save(any(Article.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ResponseEntity<?> response = tribuneController.likeArticle(articleId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        verify(articleRepository).save(argThat(a -> a.getLikeCount() == 11));
    }

    @Test
    void updateWorkflow_ShouldUpdateStatus() {
        // Arrange
        Long articleId = 1L;
        Article article = Article.builder()
                .id(articleId)
                .type(Article.ArticleType.ARTICLE_DOCTRINAL)
                .statut(Article.WorkflowStatus.SOUMIS)
                .build();

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(articleRepository.save(any(Article.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ResponseEntity<?> response = tribuneController.updateWorkflow(articleId, Article.WorkflowStatus.PUBLIE);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        verify(articleRepository).save(argThat(a -> a.getStatut() == Article.WorkflowStatus.PUBLIE));
    }
}
