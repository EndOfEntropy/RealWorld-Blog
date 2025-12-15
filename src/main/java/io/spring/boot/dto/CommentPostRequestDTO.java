package io.spring.boot.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentPostRequestDTO(@NotBlank String body) {
	
}
