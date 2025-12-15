/**
 * 
 */
package io.spring.boot.entity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * 
 */
@Entity
@Table(name = "articles")
public class Article {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "slug")
	private String slug;
	
	@Column(name = "title", unique = true, nullable = false)
	private String title;
	
	@Column(name = "description", nullable = false)
	private String description;
	
	@Column(name = "body", nullable = false)
	private String body;
	
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	
	@Column(name = "updated_at")
	private OffsetDateTime updatedAt;
	
	@Transient
	private boolean favorited = false;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", referencedColumnName = "id", nullable = false)
	private User author;
	
	@OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Comment> article_comments = new HashSet<Comment>();
	
	@ManyToMany
	@JoinTable(
			name = "articles_favorites", 
			joinColumns = @JoinColumn(name = "article_id"),
			inverseJoinColumns = @JoinColumn(name = "user_id"))
	private Set<User> favoritedBy = new HashSet<User>();
	
	@ManyToMany
	@JoinTable(name = "article_tags",
	    joinColumns = @JoinColumn(name = "article_id"),
	    inverseJoinColumns = @JoinColumn(name = "tag_id"))
	private Set<Tag> tags = new HashSet<>();
	
	// Default constructor for JPA
	protected Article() {
	}

	public Article(String title, String description, String body) {
		this.title = title;
		this.slug = generateSlug(title);
		this.description = description;
		this.body = body;
		this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}
	
	public Article(String title, String description, String body, Set<Tag> tags) {
		this.title = title;
		this.slug = generateSlug(title);
		this.description = description;
		this.body = body;
		this.tags = tags;
		this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}
	
	public Article(Long id, String title, String description, String body) {
		this.id = id;
		this.title = title;
		this.slug = generateSlug(title);
		this.description = description;
		this.body = body;
		this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}
	
    // Helper method to generate slug from title
    public static String generateSlug(String title) {
        // Check if title is null or empty to prevent invalid slugs
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        return title.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-")         // Replace spaces with dashes
                .replaceAll("-+", "-");          // Replace multiple dashes with single dash
    }

    public Article favoriteArticle(User user) {
    	favoritedBy.add(user);
    	return updateFavoriteByUser(user);
    }
    
    public Article unfavoriteArticle(User user) {
    	favoritedBy.remove(user);
    	return updateFavoriteByUser(user);
    }
    
    public Article updateFavoriteByUser(User user) {
        favorited = favoritedBy.contains(user);
        return this;
    }
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
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

	public Set<User> getFavoritedBy() {
		return favoritedBy;
	}

	public void setFavoritedBy(Set<User> favoritedBy) {
		this.favoritedBy = favoritedBy;
	}
	
	public int getFavoritedCount() {
		return favoritedBy.size();
	}
	
	// Helper method to get the list of tag names
    public Set<String> getTagList() {
    	return tags.stream().map(Tag::getName).collect(Collectors.toSet());
    }
    
    public boolean isFavorited() {
        return favorited;
    }

	@Override
	public String toString() {
		return "Article [id=" + id + ", slug=" + slug + ", title=" + title + ", description=" + description + ", body="
				+ body + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", author=" + author
				+ ", article_comments=" + article_comments + ", favoritedBy=" + favoritedBy + ", tags=" + tags + "]";
	}

}
