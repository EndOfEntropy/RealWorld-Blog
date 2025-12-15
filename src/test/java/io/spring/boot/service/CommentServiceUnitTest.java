package io.spring.boot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.spring.boot.entity.Article;
import io.spring.boot.entity.Comment;
import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;
import io.spring.boot.repository.CommentRepository;

@ExtendWith(MockitoExtension.class)
public class CommentServiceUnitTest {

	@Mock
	private CommentRepository commentRepository;
	@Mock
	private ArticleService articleService;
	
	@InjectMocks
	private CommentService commentService;
	
	private Article article;
	private User author;
	private Comment comment;
	
	@BeforeEach
	public void setup() {
        author = new User("user@gmail.com", new Profile("username"));
		article = new Article("title", "desc", "body");
		article.setAuthor(author);
		
		comment = new Comment(1L, "comment");
		comment.setArticle(article);
		comment.setAuthor(author);
	}
	
	
	@Test
	public void saveCommentTest() {
		given(commentRepository.save(any(Comment.class))).willReturn(comment);
		
		Comment result = commentService.createComment(author, article.getSlug(), article.getBody());
		
		assertThat(result).isNotNull();
		assertThat(result.getBody()).isEqualTo("comment");
		verify(commentRepository).save(any(Comment.class));
	}
	
	@Test
	public void deleteCommentTest() {
        given(commentRepository.existsById(comment.getId())).willReturn(true);
        willDoNothing().given(commentRepository).deleteById(comment.getId());

        commentService.deleteCommentById(comment.getId());

        verify(commentRepository).existsById(comment.getId());
        verify(commentRepository).deleteById(comment.getId());
	}
	
	@Test
	public void findAllCommentsTest() {
		given(commentRepository.findBySlug(article.getSlug())).willReturn(Set.of(comment));
		
		Set<Comment> result = commentService.findAllComments(article.getSlug());
		
		assertThat(result).hasSize(1);
		assertThat(result).extracting(Comment::getBody).containsExactlyInAnyOrder("comment");
        verify(commentRepository).findBySlug(article.getSlug());
	}
}
