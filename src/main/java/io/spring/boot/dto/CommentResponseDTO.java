package io.spring.boot.dto;

import java.time.OffsetDateTime;

import io.spring.boot.entity.Comment;
import io.spring.boot.entity.Profile;
import jakarta.validation.constraints.NotNull;

public class CommentResponseDTO {
	
	@NotNull
	private final Long id;
	@NotNull
	private final OffsetDateTime createdAt;
	@NotNull
	private final OffsetDateTime updatedAt;
	@NotNull
	private final String body;
	@NotNull
	private final AuthorResponseDTO author;
	
	public CommentResponseDTO(Comment comment, Profile profile, boolean following) {
		this.id = comment.getId();
		this.createdAt = comment.getCreatedAt();
		this.updatedAt = comment.getUpdatedAt();
		this.body = comment.getBody();
		this.author = new AuthorResponseDTO(profile, following);
	}

	public Long getId() {
		return id;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public String getBody() {
		return body;
	}

	public AuthorResponseDTO getAuthor() {
		return author;
	}

}
