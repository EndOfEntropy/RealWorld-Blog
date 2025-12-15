package io.spring.boot.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.spring.boot.controller.wrappers.MultipleCommentsResponse;
import io.spring.boot.controller.wrappers.SingleCommentPostRequest;
import io.spring.boot.controller.wrappers.SingleCommentResponse;
import io.spring.boot.dto.CommentResponseDTO;
import io.spring.boot.entity.Comment;
import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;
import io.spring.boot.service.CommentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/articles")
public class CommentRestController {

	@Autowired
	private CommentService commentService;
	
	@PostMapping("/{slug}/comments")
	public ResponseEntity<SingleCommentResponse> createComment(@AuthenticationPrincipal User user, 
													@PathVariable String slug, @Valid @RequestBody SingleCommentPostRequest request) {
		if(user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
		}
		Comment savedComment = commentService.createComment(user, slug, request.comment().body());
		boolean following = user != null && user.getFollowedUsers().stream().anyMatch(u -> u.getId().equals(savedComment.getAuthor().getId()));
		
		CommentResponseDTO response = new CommentResponseDTO(savedComment, savedComment.getAuthor().getProfile(), following);
		return ResponseEntity.status(HttpStatus.CREATED).body(new SingleCommentResponse(response));
	}
	
	@GetMapping("/{slug}/comments")
	public ResponseEntity<MultipleCommentsResponse> findAllComments(@AuthenticationPrincipal User user, @PathVariable String slug){
		Set<Comment> comments = commentService.findAllComments(slug);
		List<CommentResponseDTO> dtos = comments.stream().
				map(c -> { //multi-statement lambda
					Profile author = user != null ? c.getAuthor().getProfile() : null;
			        boolean following  = user != null && user.getFollowedUsers().stream().anyMatch(u -> u.getId().equals(c.getAuthor().getId()));
			        return new CommentResponseDTO(c, author, following);
				})
				.toList();
		return ResponseEntity.ok(new MultipleCommentsResponse(dtos));
	}
	
	@DeleteMapping("/{slug}/comments/{id}")
	public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal User user, @PathVariable String slug, @PathVariable Long id) {
		if(user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
		}
		
		Comment comment = commentService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	    if (!comment.getAuthor().getId().equals(user.getId())) {
	        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
	    }
	    commentService.deleteCommentById(id);
		
		return ResponseEntity.noContent().build();
	}
}