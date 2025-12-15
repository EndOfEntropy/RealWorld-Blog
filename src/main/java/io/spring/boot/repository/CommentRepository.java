package io.spring.boot.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.spring.boot.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository <Comment, Long>{

    @Query("SELECT c FROM Comment c WHERE c.article.slug = :slug ORDER BY c.createdAt DESC")
    Set<Comment> findBySlug(@Param("slug") String slug);
}