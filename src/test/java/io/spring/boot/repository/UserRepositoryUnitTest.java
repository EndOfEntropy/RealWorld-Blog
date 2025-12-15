/**
 * 
 */
package io.spring.boot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;

/**TODO
 * Figure out nested java objects @Embedded and @Embeddable. Done
 * Figure out nested java objects @OneToMany and @Join work together.
 * Thorough example:
 * https://medium.com/@bectorhimanshu/efficient-nested-entity-retrieval-in-spring-boot-with-custom-finder-methods-e3dbd923b089
 * The best way to map a @OneToMany
 * https://vladmihalcea.com/the-best-way-to-map-a-onetomany-association-with-jpa-and-hibernate/
 */
@DataJpaTest(showSql = false)
@Transactional
public class UserRepositoryUnitTest {

	@Autowired
	private UserRepository userRepository;
	
	// helper method, which saves a user with the specified email and username.
	private User createTestUser(String email, String username) {
		Profile profile = new Profile(username, "Bio for " + username, "https://image.com", false);
		User user = new User(email, profile);
		
		return userRepository.save(user);
	}
	
	@Test
	void testFindById() {
        User user = createTestUser("john.doe@gmail.com", "johndoe");
        
        Optional<User> result = userRepository.findById(user.getId());
        
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john.doe@gmail.com");
        assertThat(result.get().getProfile().getUsername()).isEqualTo("johndoe");
	}
	
	@Test
	void testSave() {
		User user = new User("newuser@gmail.com", new Profile("newuser", "I like dogs", "https://www.com", false));
        // save a few books, ID auto increase, expect 1, 2, 3, 4
        User savedUser= userRepository.save(user);
        
        Optional<User> result = userRepository.findById(user.getId());
        
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
        assertThat(result.get().getEmail()).isEqualTo("newuser@gmail.com");
        assertThat(result.get().getProfile().getImage()).isEqualTo("https://www.com");
	}
	
	@Test
	void testFindFirstByEmail() {
		User user = createTestUser("john.doe@gmail.com", "johndoe");
		
		Optional<User> result = userRepository.findFirstByEmail("john.doe@gmail.com");
        
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john.doe@gmail.com");
        assertThat(result.get().getProfile().getUsername()).isEqualTo("johndoe");
	}
	
	@Test
	void testFindfindFirstByProfileUsername() {
		User user = createTestUser("john.doe@gmail.com", "johndoe");
		
		Optional<User> result = userRepository.findFirstByProfileUsername("johndoe");
		
		assertThat(result).isPresent();
		assertThat(result.get().getId()).isEqualTo(user.getId());
		assertThat(result.get().getEmail()).isEqualTo("john.doe@gmail.com");
        assertThat(result.get().getProfile().getUsername()).isEqualTo("johndoe");
	}
	
	@Test
	void testUpdate() {
		User user = createTestUser("john.doe@gmail.com", "johndoe");
        
        // update email and name
		user.setEmail("updated@gmail.com");
		user.getProfile().setUsername("MisterT");
        // update
        userRepository.save(user);
        
        Optional<User> updatedUser = userRepository.findFirstByEmail("updated@gmail.com");
        
        System.out.println("User Id = " + updatedUser.get().getId());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getId()).isEqualTo(user.getId());
        assertThat(updatedUser.get().getEmail()).isEqualTo("updated@gmail.com");
        assertThat(updatedUser.get().getProfile().getUsername()).isEqualTo("MisterT");
	}
	
	@Test
	void testFindAll() {
		createTestUser("user1@gmail.com", "user1");
        createTestUser("user2@gmail.com", "user2");
		
		List<User> users = userRepository.findAll();
		
        assertThat(users).hasSize(2);
	}
	
	@Test
	void testDeleteById() {
		User user = createTestUser("deleteuser@gmail.com", "deleteuser");
		
        userRepository.deleteById(user.getId());
        Optional<User> deletedUser = userRepository.findById(user.getId());

        assertThat(deletedUser).isEmpty();
	}
	
	@Test
	void testFollowUser() {
		User follower = createTestUser("follower@gmail.com", "follower");
        User followee = createTestUser("followee@gmail.com", "followee");
        
        follower.followUser(followee);
        userRepository.save(follower);
        
        Optional<User> updatedFollower = userRepository.findById(follower.getId());
		
		assertThat(updatedFollower.get().getFollowedUsers()).hasSize(1);
		assertThat(updatedFollower.get().getFollowedUsers().stream().map(User::getId)).containsExactly(followee.getId());
	}
	
	@Test
	void testUnfollowUser() {
		User follower = createTestUser("follower@gmail.com", "follower");
        User followee = createTestUser("followee@gmail.com", "followee");
        
        follower.followUser(followee);
        userRepository.save(follower);
		
        follower.unfollowUser(followee);
        userRepository.save(follower);
        
        Optional<User> updatedFollower = userRepository.findById(follower.getId());
        assertThat(updatedFollower.get().getFollowedUsers()).isEmpty();
	}
	
}
