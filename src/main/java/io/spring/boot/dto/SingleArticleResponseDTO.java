package io.spring.boot.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import io.spring.boot.entity.Article;

public class SingleArticleResponseDTO {

	private final String slug;
	private final String title;
	private final String description;
	private final String body;
	private final List<String> tagList;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime updatedAt;
	private final boolean favorited;
	private final int favoritesCount;
	private final AuthorResponseDTO author;
	
	public SingleArticleResponseDTO(Article article, boolean favorited, boolean following) {
		this.slug = article.getSlug();
		this.title = article.getTitle();
		this.description = article.getDescription();
		this.body = article.getBody();
		this.tagList = article.getTagList().stream().sorted().toList();
		this.createdAt = article.getCreatedAt();
		this.updatedAt = article.getUpdatedAt();
		this.favorited = favorited;
		this.favoritesCount = article.getFavoritedCount();
		this.author = new AuthorResponseDTO(article.getAuthor().getProfile(), following);
	}

	public String getSlug() {
		return slug;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getBody() {
		return body;
	}

	public List<String> getTagList() {
		return tagList;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public boolean isFavorited() {
		return favorited;
	}

	public int getFavoritesCount() {
		return favoritesCount;
	}

	public AuthorResponseDTO getAuthor() {
		return author;
	}

}
