package com.lexdata.tribune.repository;

import com.lexdata.tribune.models.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("SELECT a FROM Article a WHERE a.statut = 'PUBLIE' " +
           "AND (:query IS NULL OR LOWER(a.titre) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.resume) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:theme IS NULL OR :theme MEMBER OF a.themes) " +
           "AND (:type IS NULL OR a.type = :type)")
    Page<Article> searchArticles(@Param("query") String query, 
                                @Param("theme") String theme, 
                                @Param("type") Article.ArticleType type, 
                                Pageable pageable);

    Page<Article> findByStatut(Article.WorkflowStatus statut, Pageable pageable);
}
