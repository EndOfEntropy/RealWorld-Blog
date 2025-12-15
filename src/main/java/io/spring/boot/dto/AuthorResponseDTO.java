package io.spring.boot.dto;

import io.spring.boot.entity.Profile;

public class AuthorResponseDTO {

	private final String username;
    private final String bio;
    private final String image;
    private final boolean following;
    
	public AuthorResponseDTO(Profile profile, boolean following) {
		this.username = profile != null ? profile.getUsername() : "null";
		this.bio = profile != null ? profile.getBio() : "null";
		this.image = profile != null ? profile.getImage()  : "null";
		this.following = following;
	}

	public String getUsername() {
		return username;
	}

	public String getBio() {
		return bio;
	}

	public String getImage() {
		return image;
	}

	public boolean isFollowing() {
		return following;
	}
    
}
