/**
 * 
 */
package io.spring.boot.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.spring.boot.controller.wrappers.MultipleArticlesResponse;
import io.spring.boot.controller.wrappers.SingleArticlePostRequest;
import io.spring.boot.controller.wrappers.SingleArticlePutRequest;
import io.spring.boot.controller.wrappers.SingleArticleResponse;
import io.spring.boot.dto.MultipleArticlesResponseDTO;
import io.spring.boot.dto.SingleArticleResponseDTO;
import io.spring.boot.entity.Article;
import io.spring.boot.entity.Tag;
import io.spring.boot.entity.User;
import io.spring.boot.service.ArticleService;
import jakarta.validation.Valid;

/**
 * 
 */
@RestController
@RequestMapping("/api/articles")
class ArticleRestController {

	private final ArticleService articleService;

	@Autowired
	public ArticleRestController(ArticleService articleService) {
		this.articleService = articleService;
	}
	
	@GetMapping("/{slug}")
	public ResponseEntity<SingleArticleResponse> findArticleBySlug(@AuthenticationPrincipal User user, @PathVariable String slug) {
		Article article = articleService.findArticleBySlug(slug);
	    boolean favorited = user != null && article.getFavoritedBy().stream().anyMatch(u -> u.getId().equals(user.getId()));
	    boolean following = user != null && user.getFollowedUsers().stream().anyMatch(u -> u.getId().equals(article.getAuthor().getId()));
	    
		SingleArticleResponseDTO response = new SingleArticleResponseDTO(article, favorited, following);
		return ResponseEntity.ok(new SingleArticleResponse(response));
	}
	
	@PostMapping()
	public ResponseEntity<SingleArticleResponse> createArticle(@AuthenticationPrincipal User user, 
																@Valid @RequestBody SingleArticlePostRequest request){
		if(user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
		}
		Article article = new Article(request.article().getTitle(), request.article().getDescription(), request.article().getBody(), 
										request.article().getTagList().stream().map(Tag::new).collect(Collectors.toSet()));
		article.setAuthor(user);
		Article savedArticle = articleService.saveArticle(article);
		SingleArticleResponseDTO response = new SingleArticleResponseDTO(savedArticle, false, false);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(new SingleArticleResponse(response));
	}
	
	@DeleteMapping("/{slug}")
	public ResponseEntity<Void>deleteArticle(@AuthenticationPrincipal User user, @PathVariable String slug){
		Article article = articleService.findArticleBySlug(slug);
		if(!article.getAuthor().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
		}
		articleService.deleteByArticleSlug(slug);
		
		return ResponseEntity.noContent().build();
	}
	
	@PutMapping("/{slug}")
	public ResponseEntity<SingleArticleResponse>updateArticle(@AuthenticationPrincipal User user, 
																@PathVariable String slug, @RequestBody SingleArticlePutRequest request){
		Article article = articleService.findArticleBySlug(slug);
		if(!article.getAuthor().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
		}
		Article updatedArticle = articleService.updateArticle(slug, request.article());
	    boolean favorited = updatedArticle.getFavoritedBy().stream().anyMatch(u -> u.getId().equals(user.getId()));
	    boolean following = user.getFollowedUsers().stream().anyMatch(f -> f.getId().equals(updatedArticle.getAuthor().getId()));
		
		SingleArticleResponseDTO response = new SingleArticleResponseDTO(updatedArticle, favorited, following);
		return ResponseEntity.ok(new SingleArticleResponse(response));
	}
	
	@GetMapping()
	public ResponseEntity<MultipleArticlesResponse> findArticles(
									        @AuthenticationPrincipal User user,
									        @RequestParam(required = false) String tag,
									        @RequestParam(required = false) String author,
									        @RequestParam(required = false) String favorited,
									        @RequestParam(defaultValue = "20") int limit,
									        @RequestParam(defaultValue = "0") int offset) {

	    Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by("createdAt").descending());
	    Page<Article> page = articleService.findArticleByCriteria(tag, author, favorited, pageable);

		List<MultipleArticlesResponseDTO> dtos = page.getContent().stream()
			.map(a -> { //multi-statement lambda
            	boolean favoritedByUser = user != null && a.getFavoritedBy().stream().anyMatch(u -> u.getId().equals(user.getId()));
            	boolean followingAuthor = user != null && user.getFollowedUsers().stream().anyMatch(f -> f.getId().equals(a.getAuthor().getId()));

	            return new MultipleArticlesResponseDTO(a, favoritedByUser, followingAuthor);
	        })
	        .toList();

	    return ResponseEntity.ok(new MultipleArticlesResponse(dtos, page.getTotalElements()));
	}
	
	@GetMapping("/feed")
	public ResponseEntity<MultipleArticlesResponse>findFeed(
											@AuthenticationPrincipal User user, 
											@RequestParam(defaultValue = "20") int limit,
											@RequestParam(defaultValue = "0") int offset){
		if (user == null) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
	    }
		Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by("createdAt").descending());
		Page<Article> page = articleService.findFeed(user.getId(), pageable);
		
		List<MultipleArticlesResponseDTO> dtos = page.getContent().stream()
				.map(a -> { //multi-statement lambda
	            	boolean favoritedByUser = user != null && a.getFavoritedBy().stream().anyMatch(u -> u.getId().equals(user.getId()));
	            	boolean followingAuthor = user != null && user.getFollowedUsers().stream().anyMatch(f -> f.getId().equals(a.getAuthor().getId()));

		            return new MultipleArticlesResponseDTO(a, favoritedByUser, followingAuthor);
		        })
		        .toList();
		
		return ResponseEntity.ok(new MultipleArticlesResponse(dtos, page.getTotalElements()));
	}
	
	@PostMapping("/{slug}/favorite")
	public ResponseEntity<SingleArticleResponse>favoriteArticle(@AuthenticationPrincipal User user, @PathVariable String slug){
		if(user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
		}
		Article article = articleService.favoriteArticle(user.getId(), slug);
	    boolean favorited = article.getFavoritedBy().stream().anyMatch(u -> u.getId().equals(user.getId()));
	    boolean following = user.getFollowedUsers().stream().anyMatch(u -> u.getId().equals(article.getAuthor().getId()));
	    
		SingleArticleResponseDTO response = new SingleArticleResponseDTO(article, favorited, following);

		return ResponseEntity.ok(new SingleArticleResponse(response));
	}
	
	@DeleteMapping("/{slug}/favorite")
	public ResponseEntity<SingleArticleResponse>unfavoriteArticle(@AuthenticationPrincipal User user, @PathVariable String slug){
		if(user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
		}
		Article article = articleService.unfavoriteArticle(user.getId(), slug);
	    boolean favorited = article.getFavoritedBy().stream().anyMatch(u -> u.getId().equals(user.getId()));
	    boolean following = user.getFollowedUsers().stream().anyMatch(u -> u.getId().equals(article.getAuthor().getId()));
	    
		SingleArticleResponseDTO response = new SingleArticleResponseDTO(article, favorited, following);
		
		return ResponseEntity.ok(new SingleArticleResponse(response));
	}
	
}
