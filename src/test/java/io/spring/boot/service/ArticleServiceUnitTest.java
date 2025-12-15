package io.spring.boot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.spring.boot.dto.ArticlePutRequestDTO;
import io.spring.boot.entity.Article;
import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;
import io.spring.boot.repository.ArticleRepository;
import io.spring.boot.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceUnitTest {

	@Mock
	private ArticleRepository articleRepository;
	@Mock
    private UserRepository userRepository;
	
	@InjectMocks
	private ArticleService articleService;
	
	private User author;
	private User follower;
	private Article article;
	
	@BeforeEach
	private void setUp() {
        author = new User(1L, "user@gmail.com", new Profile("username"));
        follower = new User(2L, "johndoe@gmail.com", new Profile("johndoe"));
		article = new Article(1L, "title", "description", "body");
		article.setAuthor(author);
	}

	@Test
	void testSaveArticle() {
		// precondition
        given(articleRepository.save(article)).willReturn(article);
		
		// action
		Article result = articleService.saveArticle(article);
		
		// verify
		assertThat(result).isNotNull();
		assertThat(result.getTitle()).isEqualTo("title");
		assertThat(result.getBody()).isEqualTo("body");
		verify(articleRepository).save(article);
	}
	
	@Test
	void testFindArticleById() {
		// precondition
		given(articleRepository.findById(1L)).willReturn(Optional.of(article));
		
		// action
		Article result = articleService.findArticleById(1L);
		
		// verify
		assertThat(result).isNotNull();
		assertThat(result.getTitle()).isEqualTo("title");
		assertThat(result.getBody()).isEqualTo("body");
		verify(articleRepository).findById(1L);
	}
	
	@Test
	void testUpdateArticle() {
		// Given
	    Article existingArticle = new Article(3L, "title", "description", "body");
	    existingArticle.setAuthor(author); // Set author to match updateArticle behavior
	    ArticlePutRequestDTO request = new ArticlePutRequestDTO("title2", "description2", "body2");

	    given(articleRepository.findBySlug("title2")).willReturn(Optional.of(existingArticle));
	    given(articleRepository.save(existingArticle)).willAnswer(invocation -> {
	        Article article = invocation.getArgument(0);
	        article.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC)); // Simulate repository save behavior
	        return article;
	    });

	    // When
	    Article result = articleService.updateArticle("title2", request);

	    // Then
	    assertThat(result.getUpdatedAt()).isNotNull();
	    verify(articleRepository).findBySlug("title2");
	    verify(articleRepository).save(existingArticle);
	}
	
	
	@Test
	void testDeleteById() {
		// precondition
		given(articleRepository.existsById(1L)).willReturn(true);
		willDoNothing().given(articleRepository).deleteById(1L);
		
		// action
		articleService.deleteArticleById(1L);
		
		// verify
		verify(articleRepository).existsById(1L);
		verify(articleRepository).deleteById(1L);
	}
	
	@Test
	void testDeleteBySlug() {
	    // precondition
	    String slug = "test-slug";
	    given(articleRepository.existsBySlug(slug)).willReturn(true);
	    willDoNothing().given(articleRepository).deleteBySlug(slug);

	    // action
	    articleService.deleteByArticleSlug(slug);

	    // verify
	    verify(articleRepository).existsBySlug(slug);
	    verify(articleRepository).deleteBySlug(slug);
	}
	
	@Test
	void testFindBySlug() {
		// precondition
		Article articleNew = new Article(1L, "Title with long slug", "desc slug", "body slug");
		String slugNew = "title-with-long-slug";
		given(articleRepository.findBySlug("title-with-long-slug")).willReturn(Optional.of(articleNew));
		
		// action
		Article result = articleService.findArticleBySlug(slugNew);
		
		// verify
		assertThat(result).isNotNull();
		assertThat(result.getTitle()).isEqualTo("Title with long slug");
		assertThat(result.getSlug()).isEqualTo(slugNew);
		verify(articleRepository).findBySlug(slugNew);
	}
	
	
	@Test
	void testFindAllArticles() {
		// precondition
		Article article2 = new Article(2L, "title2", "description2", "body2");
		article2.setAuthor(author);
        follower.followUser(author);
        given(articleRepository.findAll()).willReturn(List.of(article, article2));
		
		// action
		List<Article >result = articleService.findAll();
		
		// verify
		assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(article, article2);
		verify(articleRepository).findAll();
	}
	
	@Test
	void testFindFeed() {
		// precondition
		Article article2 = new Article(2L, "title2", "description2", "body2");
		article2.setAuthor(author);
        follower.followUser(author);
        Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(article, article2);
        Page<Article> page = new PageImpl<>(articles, pageable, articles.size());
        given(articleRepository.findFeed(follower.getId(), pageable)).willReturn(page);
        
        // action
        Page<Article> result = articleService.findFeed(follower.getId(), pageable);
        
        // verify
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactlyInAnyOrder(article, article2);
        assertThat(result.getContent().get(0).getAuthor().getId()).isEqualTo(author.getId());
		verify(articleRepository).findFeed(follower.getId(), pageable);
	}
	
	@Test
	void testFindByCriteria_Success() {
		// precondition
		Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(article);
        Page<Article> page = new PageImpl<>(articles, pageable, articles.size());
        given(articleRepository.findByCriteria("java", null, null, pageable)).willReturn(page);
        
        // action
        Page<Article> result = articleService.findArticleByCriteria("java", null, null, pageable);
        
        // verify
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).containsExactlyInAnyOrder(article);
		verify(articleRepository).findByCriteria("java", null, null, pageable);
	}
	
	@Test
	void testFavoriteArticle() {
		// precondition
		Article article2 = new Article(2L, "title2", "description2", "body2");
		String slug2 = "title2";
		given(userRepository.findById(follower.getId())).willReturn(Optional.of(follower));
		given(articleRepository.findBySlug(slug2)).willReturn(Optional.of(article2));
		given(articleRepository.save(article2)).willReturn(article2);
		
		// action
		Article result = articleService.favoriteArticle(follower.getId(), slug2);
		
		// verify
		assertThat(result).isNotNull();
		assertThat(result.getTitle()).isEqualTo("title2");
		assertThat(result.getBody()).isEqualTo("body2");
		assertThat(result.getFavoritedBy()).contains(follower); // Verify user is in favoritedBy
		verify(userRepository).findById(follower.getId());
		verify(articleRepository).findBySlug(slug2);
		verify(articleRepository).save(article2);
	}
	
	@Test
	void testUnfavoriteArticle() {
		// precondition
		Article article2 = new Article(2L, "title2", "description2", "body2");
		String slug2 = "title2";
		article2.favoriteArticle(follower);
		given(userRepository.findById(follower.getId())).willReturn(Optional.of(follower));
		given(articleRepository.findBySlug(slug2)).willReturn(Optional.of(article2));
		given(articleRepository.save(article2)).willReturn(article2);
		
		// action
		Article result = articleService.unfavoriteArticle(follower.getId(), slug2);
		
		// verify
		assertThat(result).isNotNull();
		assertThat(result.getTitle()).isEqualTo("title2");
		assertThat(result.getBody()).isEqualTo("body2");
		assertThat(result.getFavoritedBy()).isEmpty();
		verify(userRepository).findById(follower.getId());
		verify(articleRepository).findBySlug(slug2);
		verify(articleRepository).save(article2);
	}
	
}
