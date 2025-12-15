/**
 * 
 */
package io.spring.boot.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;

/**
 * ❌ JPA serialization issues: @Embeddable + Jackson annotations can conflict during JPA operations
 * ❌ Tight coupling: Entity tied to API contract
 * ❌ @Transient fields: following is @Transient - ensure Jackson serializes it
 * ❌ Debugging: Harder to debug JSON structure in entity
 * create a DTO is issues are noted in the service tests
 */
@Embeddable
@JsonTypeName("profile")
@JsonTypeInfo(include = As.WRAPPER_OBJECT, use = Id.NAME)
public class Profile {
	
	@Column(name = "username", unique = true, nullable = false)
	private String username;
	
	@Column(name = "bio")
	private String bio;
	
	@Column(name = "image")
	private String image;
	
	@Transient
	private boolean following;
	

	public Profile(String username) {
		this(username, null, null, false);
	}
	
	// JPA no-arg constructor
	protected Profile() {
	}
	
	public Profile(String username, String bio, String image, boolean following) {
		this.username = username;
		this.bio = bio;
		this.image = image;
		this.following = following;
	}
	
	public Profile(String username, String bio, String image) {
		this.username = username;
		this.bio = bio;
		this.image = image;
		this.following = false;
	}
	
	public Profile withFollowing(boolean following) {
		this.following = following;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public boolean isFollowing() {
		return following;
	}

	public void setFollowing(boolean following) {
		this.following = following;
	}

	@Override
	public String toString() {
		return "Profile [username=" + username + ", bio=" + bio + ", image=" + image + ", following=" + following + "]";
	}

}
