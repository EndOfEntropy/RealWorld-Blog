package io.spring.boot.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.spring.boot.entity.Article;
import io.spring.boot.entity.Comment;
import io.spring.boot.entity.User;
import io.spring.boot.repository.CommentRepository;

@Service
public class CommentService {

	@Autowired
	CommentRepository commentRepository;
	
	@Autowired
	ArticleService articleService;
	
	@Transactional(readOnly = true)
	public Optional<Comment> findById(Long id) {
		return commentRepository.findById(id);
	}
	
	@Transactional
	public Comment createComment(User user, String slug, String body) {
		if (body == null || body == null || body.trim().isEmpty()) {
	        throw new IllegalArgumentException("Body cannot be null or empty");
	    }
		Article article = articleService.findArticleBySlug(slug);
	    Comment comment = new Comment(body);
	    comment.setAuthor(user);
	    comment.setArticle(article);
		
		return commentRepository.save(comment);
	}
	
	@Transactional
	public void deleteCommentById(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new IllegalArgumentException("Comment with ID " + id + " does not exist");
        }
        commentRepository.deleteById(id);
	}
	
	@Transactional(readOnly = true)
	public Set<Comment> findAllComments(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Slug cannot be null or empty");
        }
        
        return commentRepository.findBySlug(slug);
	}
}
