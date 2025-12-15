package io.spring.boot.entity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "comments")
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "body", nullable = false)
	private String body;
	
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	
	@Column(name = "updated_at")
	private OffsetDateTime updatedAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="author_id", referencedColumnName = "id", nullable = false)
	private User author;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="article_id", referencedColumnName = "id", nullable = false)
	private Article article;
	
	protected Comment(){
	}

	public Comment(Long id, String body) {
		this.id = id;
		this.body = body;
		this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}
	
	public Comment(String body) {
		this.body = body;
		this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}
	
//	@PreUpdate
//	public void preUpdate() {
//	    this.updatedAt = LocalDateTime.now();
//	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public Article getArticle() {
		return article;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

	@Override
	public String toString() {
		return "Comment [id=" + id + ", body=" + body + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
				+ ", author=" + author + ", article=" + article + "]";
	}
	
}
