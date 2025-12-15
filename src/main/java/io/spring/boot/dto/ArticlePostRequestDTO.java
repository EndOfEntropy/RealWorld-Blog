package io.spring.boot.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;

public class ArticlePostRequestDTO {

	@NotBlank
	private final String title;
	@NotBlank
	private final String description;
	@NotBlank
	private final String body;
	
	private final Set<String> tagList;
	
	public ArticlePostRequestDTO(String title, String description, String body, Set<String> tagList) {
		this.title = title;
		this.description = description;
		this.body = body;
		this.tagList = tagList;
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

    public Set<String> getTagList() {
        return tagList;
    }
}
