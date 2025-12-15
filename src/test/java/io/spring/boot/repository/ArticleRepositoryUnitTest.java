package io.spring.boot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import io.spring.boot.entity.Article;
import io.spring.boot.entity.Profile;
import io.spring.boot.entity.Tag;
import io.spring.boot.entity.User;


@DataJpaTest(showSql = false)
@Transactional
public class ArticleRepositoryUnitTest {

	@Autowired
	private ArticleRepository articleRepository;
	
	/**The TestEntityManager is a test-specific wrapper around the JPA EntityManager, tailored for use in @DataJpaTest environments. 
	 * It simplifies common test operations, such as persisting, merging, or finding entities, without relying on the repository layer.
	 */
	@Autowired
    private TestEntityManager entityManager;
	
	private User author;
	private User follower;
	
	@BeforeEach
	private void setUp() {
        Profile profile = new Profile("username");
        author = new User("user@gmail.com", profile);
        entityManager.persist(author); // Persist User with Profile once per test
        
        Profile followerProfile = new Profile("johndoe");
        follower = new User("johndoe@gmail.com", followerProfile);
        entityManager.persist(follower);
	}
	
	private Article createDefaultArticle(User author) {
		Article article = new Article("Like light to the flies", "Tyler Durden", "Grave of the Fireflies");
		article.setAuthor(author); // Use persisted User
		return articleRepository.save(article);
	}
	
	private Article createNewArticle(User author, String title) {
		Article article = new Article(title, "", "");
		article.setAuthor(author); // Use persisted User
		return articleRepository.save(article);
	}
	
	@Test
	void testSave() {
		Article result = createDefaultArticle(author);
		
		assertThat(result).isNotNull();
		assertThat(result.getBody()).isEqualTo("Grave of the Fireflies");
		assertThat(result.getSlug()).isEqualTo("like-light-to-the-flies");
//		assertThat(result.getTagList()).hasSize(3);
	}
	
	@Test
	void testFindById() {
		Article article = createDefaultArticle(author);
		
		Optional<Article> result = articleRepository.findById(article.getId());
		assertThat(result).isPresent();
		assertThat(result.get().getTitle()).isEqualTo("Like light to the flies");
		assertThat(result.get().getDescription()).isEqualTo("Tyler Durden");
	}
	
	@Test
	void testUpdate() {
		Article article = createDefaultArticle(author);
		article.setTitle("New Title");
		article.setBody("New Body");
		
		Article result = articleRepository.save(article);
		assertThat(result).isNotNull();
		assertThat(result.getTitle()).isEqualTo("New Title");
		assertThat(result.getDescription()).isEqualTo("Tyler Durden");
		assertThat(result.getBody()).isEqualTo("New Body");
	}
	
	@Test
	void testDeleteById() {
		Article article = createDefaultArticle(author);
		
		articleRepository.deleteById(article.getId());
		Optional<Article> result = articleRepository.findById(article.getId());
		assertThat(result).isEmpty();
	}
	
	@Test
	void testExistBySlug() {
		Article article = createDefaultArticle(author);
		
		boolean bool = articleRepository.existsBySlug(article.getSlug());
		
		assertThat(article).isNotNull();
		assertThat(bool).isTrue();
	}
	
	@Test
	void testFindFirstBySlug() {
		Article article = createDefaultArticle(author);
		
		Optional<Article> result = articleRepository.findBySlug(article.getSlug());
		
		assertThat(result).isPresent();
		assertThat(result.get().getTitle()).isEqualTo("Like light to the flies");
		assertThat(result.get().getDescription()).isEqualTo("Tyler Durden");
	}

	@Test
	void testDeleteBySlug() {
		Article article = createDefaultArticle(author);
		
		articleRepository.deleteBySlug(article.getSlug());
		Optional<Article> result = articleRepository.findById(article.getId());
		assertThat(result).isEmpty();
	}
	
	@Test
	void testFindFeed() {
		// Arrange: Set up follow relationship and articles
        follower.followUser(author);
        entityManager.persist(follower);
        Article article1 = createNewArticle(author, "First");
        Article article2 = createNewArticle(author, "Second");
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Article> result = articleRepository.findFeed(follower.getId(), pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactlyInAnyOrder(article1, article2);
        assertThat(result.getContent().get(0).getAuthor().getId()).isEqualTo(author.getId());
	}
	
	@Test
	void testFindByCriteria_withAuthorFilter() {
        // Arrange: Create articles by different authors
        User anotherAuthor = new User("other@gmail.com", new Profile("otheruser"));
        entityManager.persist(anotherAuthor);
        Article article = createNewArticle(anotherAuthor, "My Article");
        createDefaultArticle(author);
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Article> result = articleRepository.findByCriteria(null, "otheruser", null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAuthor().getProfile().getUsername()).isEqualTo("otheruser");
	}
	
	@Test
	void testFindByCriteria_withTagFilter() {
        // Arrange: Create articles by different authors
        User anotherAuthor = new User("other@gmail.com", new Profile("otheruser"));
        entityManager.persist(anotherAuthor);
        
        // Create and persist tags
        Tag javaTag = new Tag("Java");
        entityManager.persist(javaTag);  // Persist tag before associating it with the article
        
        Article article = createNewArticle(anotherAuthor, "Real World Endpoints");
        article.setTags(new HashSet<Tag>(Set.of(javaTag)));  // Associate tag with the article
        entityManager.persist(article); // Persist the article with tags
        
        createDefaultArticle(author);
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Article> result = articleRepository.findByCriteria("Java", null, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Real World Endpoints");
	}
	
	@Test
	void testFindByCriteria_withFavoritedByUsernameFilter() {
        // Arrange: Create articles by different authors
        User anotherAuthor = new User("other@gmail.com", new Profile("otheruser"));
        entityManager.persist(anotherAuthor);
        Article article = createNewArticle(anotherAuthor, "Real World Endpoints");
        article.favoriteArticle(anotherAuthor);
        articleRepository.save(article);
        createDefaultArticle(author);
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Article> result = articleRepository.findByCriteria(null, null, "otheruser", pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Real World Endpoints");
	}
	
}
