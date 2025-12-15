/**
 * 
 */
package io.spring.boot.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.spring.boot.entity.Article;
import io.spring.boot.entity.User;

/**
 * 
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>{
	
	Optional<Article> findBySlug(String slug);
	boolean existsBySlug(String slug);
	void deleteBySlug(String slug);
	Page<Article> findAll(Pageable pageable);	//pagination (limit and offset) handled in the controller
	
	// The repository should handle data retrieval and filtering
	@Query("SELECT a FROM Article a WHERE a.author IN (" +
	           "SELECT fu FROM User u JOIN u.followedUsers fu WHERE u.id = :userId)")
	Page<Article> findFeed(Long userId, Pageable pageable);
    
	@Query("SELECT a FROM Article a WHERE " +
	           "(:tag IS NULL OR EXISTS (SELECT 1 FROM a.tags t WHERE t.name = :tag)) AND " +
	           "(:authorUsername IS NULL OR a.author.profile.username = :authorUsername) AND " +
	           "(:favoritedByUsername IS NULL OR :favoritedByUsername IN " +
	           "(SELECT u.profile.username FROM a.favoritedBy u))")
	Page<Article> findByCriteria(String tag, String authorUsername, String favoritedByUsername, Pageable pageable);
}