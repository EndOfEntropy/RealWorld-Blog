package io.spring.boot.dto;

import io.spring.boot.entity.User;

public class UserResponseDTO {

    private final String email;
    private final String token;
    private final String username;
    private final String bio;
    private final String image;

    public UserResponseDTO(String email, String token, String username, String bio, String image) {
        this.email = email;
		this.token = token;
        this.username = username;
        this.bio = bio;
        this.image = image;
    }
    
    public UserResponseDTO(User user, String token) {
        this.email = user.getEmail();
		this.token = token;
        this.username = user.getProfile().getUsername();
        this.bio = user.getProfile().getBio();
        this.image = user.getProfile().getImage();
    }

    public String getEmail() {
        return email;
    }
    
    public String getToken() {
        return token;
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
}