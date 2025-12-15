package io.spring.boot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import io.spring.boot.entity.Article;
import io.spring.boot.entity.Comment;
import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;

@DataJpaTest(showSql = false)
@Transactional
public class CommentRepositoryUnitTest {

	@Autowired
	private CommentRepository commentRepository;
	
	/**The TestEntityManager is a test-specific wrapper around the JPA EntityManager, tailored for use in @DataJpaTest environments. 
	 * It simplifies common test operations, such as persisting, merging, or finding entities, without relying on the repository layer.
	 */
	@Autowired
	private TestEntityManager entityManager;
	
	private Article article;
	private User author;
	private Comment comment;
	
	@BeforeEach
	public void setup() {
        author = new User("user@gmail.com", new Profile("username"));
        entityManager.persist(author); // Persist User once per test
		article = new Article("title", "desc", "body");
		article.setAuthor(author);
		entityManager.persist(article); // Persist Article once per test
		
		comment = new Comment("comment");
		comment.setArticle(article);
		comment.setAuthor(author);
	}
	
	@Test
	public void saveTest() {
		Comment result = commentRepository.save(comment);
		
		assertThat(result).isNotNull();
		assertThat(result.getBody()).isEqualTo("comment");
        assertThat(result.getArticle().getId()).isEqualTo(article.getId());
        assertThat(result.getAuthor().getId()).isEqualTo(author.getId());
	}
	
	@Test
	public void deleteTest() {
		Comment savedComment = commentRepository.save(comment);
		System.out.println(savedComment);
		commentRepository.deleteById(savedComment.getId());
		Optional<Comment> result = commentRepository.findById(savedComment.getId());
		
		assertThat(result).isEmpty();
	}
	
	@Test
	public void findBySlugTest() {
		Comment savedComment = commentRepository.save(comment);
		
		Set<Comment> result = commentRepository.findBySlug(savedComment.getArticle().getSlug());
		
		assertThat(result).isNotNull();
		assertThat(result).hasSize(1);
		assertThat(result).extracting(Comment::getBody).containsExactlyInAnyOrder("comment");
		Comment found = result.iterator().next();
        assertThat(found.getCreatedAt()).isNotNull();
	}
	
    @Test
    public void findBySlugSortingTest() {
        // Create two comments with different creation times
        Comment comment1 = new Comment("first comment");
        comment1.setArticle(article);
        comment1.setAuthor(author);
        comment1.setCreatedAt(comment.getCreatedAt().minusSeconds(10)); // Older comment
        entityManager.persist(comment1);

        Comment comment2 = new Comment("second comment");
        comment2.setArticle(article);
        comment2.setAuthor(author);
        entityManager.persist(comment2);

        Set<Comment> result = commentRepository.findBySlug(article.getSlug());

        assertThat(result).hasSize(2);

     // Convert Set → ordered List (newest first)
        var ordered = result.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                .collect(Collectors.toList());

        assertThat(ordered.get(0).getBody()).isEqualTo("second comment"); // newest
        assertThat(ordered.get(1).getBody()).isEqualTo("first comment");  // oldest
    }
}
