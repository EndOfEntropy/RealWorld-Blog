/**
 * 
 */
package io.spring.boot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import io.spring.boot.entity.User;
/**
 * 
 */
public interface UserRepository extends JpaRepository<User, Long>{
	
	Optional<User> findFirstByEmail(String email);
	Optional<User> findFirstByProfileUsername(String username);
	boolean existsByEmail(String email);
    boolean existsByProfileUsername(String username);
    
    @EntityGraph(attributePaths = "followedUsers")
    Optional<User> findWithFollowedUsersByEmail(String email);
}