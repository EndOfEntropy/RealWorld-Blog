package io.spring.boot.dto;

import jakarta.validation.constraints.Email;

public class UserPutRequestDTO {

    @Email
    private final String email;
    private final String username;
    private final String password;
    private final String bio;
    private final String image;

    public UserPutRequestDTO(String email, String username, String password, String bio, String image) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.bio = bio;
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getBio() {
        return bio;
    }

    public String getImage() {
        return image;
    }
}