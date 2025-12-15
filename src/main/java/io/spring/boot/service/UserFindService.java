package io.spring.boot.service;

import java.util.Optional;

import io.spring.boot.entity.User;

// FOR REFERENCE ONLY - Used ins this Github implementation
// https://github.com/raeperd/realworld-springboot-java/blob/master/src/main/java/io/github/raeperd/realworld/domain/user/ProfileService.java

public interface UserFindService {

    Optional<User> findById(Long id);
    Optional<User> findByUsername(String userName);
}