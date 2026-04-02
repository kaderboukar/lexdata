package com.lexdata.tribune.repository;

import com.lexdata.tribune.models.ArticleComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {
    List<ArticleComment> findByArticleIdAndModereTrueOrderByDateCreationDesc(Long articleId);
}
