package io.spring.boot.dto;

public class ArticlePutRequestDTO {
	
	private final String title;
	private final String description;
	private final String body;
	
	
	public ArticlePutRequestDTO(String title, String description, String body) {
		this.title = title;
		this.description = description;
		this.body = body;
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
	
}
