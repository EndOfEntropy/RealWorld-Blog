package io.spring.boot.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.spring.boot.dto.ArticlePutRequestDTO;
import io.spring.boot.entity.Article;
import io.spring.boot.entity.Tag;
import io.spring.boot.entity.User;
import io.spring.boot.repository.ArticleRepository;
import io.spring.boot.repository.TagRepository;
import io.spring.boot.repository.UserRepository;

@Service
public class ArticleService {
	
	private final ArticleRepository articleRepository;
	private final UserRepository userRepository;
	private final TagRepository tagRepository;

	@Autowired
	public ArticleService(ArticleRepository articleRepository, UserRepository userRepository, TagRepository tagRepository) {
		this.articleRepository = articleRepository;
		this.userRepository = userRepository;
		this.tagRepository = tagRepository;
	}
	
	@Transactional(readOnly = true)
	public Article findArticleById(Long id){
		return articleRepository.findById(id).orElseThrow(()-> new NoSuchElementException("No existing article with given id: " + id));
	}

	@Transactional(readOnly = true)
	public List<Article> findAll(){
		return articleRepository.findAll();
	}
	
	@Transactional
	public Article saveArticle(Article article) {
		if (article == null || article.getTitle() == null || article.getTitle().trim().isEmpty()) {
	        throw new IllegalArgumentException("Title cannot be null or empty");
	    }
		
		if (article.getDescription() == null || article.getDescription().trim().isEmpty()) {
	        throw new IllegalArgumentException("Description cannot be null or empty");
	    }
		
		if (article.getBody() == null || article.getBody().trim().isEmpty()) {
	        throw new IllegalArgumentException("Body cannot be null or empty");
	    }
		
		if (article.getTagList() != null && !article.getTagList().isEmpty()) {
		    Set<Tag> managedTags = article.getTagList().stream()
						    		.map(String::trim)
							        .map(name -> tagRepository.findByName(name).orElseGet(() -> tagRepository.save(new Tag(name))))
							        .collect(Collectors.toSet());

		    article.setTags(managedTags);   // Set guarantees no duplicate Tag objects
		}
		
		return articleRepository.save(article);
	}
	
	@Transactional
	public Article updateArticle (String slug, ArticlePutRequestDTO dto) {
		Article existingArticle = articleRepository.findBySlug(slug).orElseThrow(
				() -> new NoSuchElementException("No existing article with given slug: " + slug));
		
		if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
			existingArticle.setTitle(dto.getTitle());
			existingArticle.setSlug(Article.generateSlug(dto.getTitle()));
	    }
		
		if (dto.getDescription() != null && !dto.getDescription().trim().isEmpty()) {
			existingArticle.setDescription(dto.getDescription());
	    }
		
		if (dto.getBody() != null && !dto.getBody().trim().isEmpty()) {
			existingArticle.setBody(dto.getBody());
		}
		
		existingArticle.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
		
		return articleRepository.save(existingArticle);
	}
	
	@Transactional
	public void deleteArticleById(Long id) {
		if(!articleRepository.existsById(id)) {
			throw new  NoSuchElementException("No existing article with given id: " + id);
		}
		articleRepository.deleteById(id);
	}
	
	@Transactional(readOnly = true)
	public Article findArticleBySlug(String slug){
		if (slug == null || slug.trim().isEmpty()) {
	        throw new IllegalArgumentException("Slug cannot be null or empty");
	    }
		
		return articleRepository.findBySlug(slug).orElseThrow(() -> new NoSuchElementException("No existing article with given slug: " + slug));
	}
	
	@Transactional
	public void deleteByArticleSlug(String slug) {
		if(!articleRepository.existsBySlug(slug)) {
			throw new NoSuchElementException("No existing article with given slug: " + slug);
		}
		
		articleRepository.deleteBySlug(slug);
	}
	
	@Transactional
	public Article favoriteArticle(Long userId, String articleSlug) {
		User user = userRepository.findById(userId).orElseThrow(
				() -> new NoSuchElementException("No existing user with given id: " + userId));
		Article article = articleRepository.findBySlug(articleSlug).orElseThrow(
				() -> new NoSuchElementException("No existing article with given slug: " + articleSlug));
		
	    if(article.getFavoritedBy().contains(user)){
	    	throw new IllegalArgumentException("User has already favorited this article");
	    }
		article.favoriteArticle(user);
		
		return articleRepository.save(article);
	}
	
	@Transactional
	public Article unfavoriteArticle(Long userId, String articleSlug) {
		User user = userRepository.findById(userId).orElseThrow(
				() -> new NoSuchElementException("No existing user with given id: " + userId));
		Article article = articleRepository.findBySlug(articleSlug).orElseThrow(
				() -> new NoSuchElementException("No existing article with given slug: " + articleSlug));
		
	    if(!article.getFavoritedBy().contains(user)){
	    	throw new IllegalArgumentException("User has not favorited this article");
	    }
		article.unfavoriteArticle(user);
		
		return articleRepository.save(article);
	}
	
	@Transactional(readOnly = true)
	public Page<Article> findFeed(Long userId, Pageable pageable){
		return articleRepository.findFeed(userId, pageable);
	}
	
	@Transactional(readOnly = true)
	public Page<Article> findArticleByCriteria(String tag, String authorUsername, String favoritedByUsername, Pageable pageable){
		return articleRepository.findByCriteria(tag, authorUsername, favoritedByUsername, pageable);
	}
	
}