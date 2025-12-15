package io.spring.boot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.spring.boot.dto.UserPutRequestDTO;
import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;
import io.spring.boot.repository.UserRepository;
import io.spring.boot.security.JwtService;

/*
Based on the findAll() results, Mockito doesnt pull data from schema.sql and data.sql
*/

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {
	
	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
    private AuthenticationManager authenticationManager;
	@Mock
	private JwtService jwtService;
	
	@InjectMocks
	private UserService userService;
	
	private User user;
	
	//Alternatively use the builder() method. Add lombok to the pom.xml and @Builder to the entity class
	@BeforeEach
	void setup() {
		user = new User(1L, "user@gmail.com", "password", new Profile("TylerDurden", "Space monkey", "https://zzz.com", false));
	}
	
	@Test
	void saveUser() {
		// precondition
		given(userRepository.save(user)).willReturn(user);
		
		// action
		User savedUser = userService.saveUser(user);
		
		// Verify
		assertThat(savedUser).isNotNull();
		assertThat(savedUser.getProfile().getUsername()).isEqualTo("TylerDurden");
		verify(userRepository).save(user);
	}
	
	@Test
	void findUserById() {
		// precondition
		given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
		
		// action
		User foundUser = userService.findUserById(user.getId());
		
		// verify
		assertThat(foundUser).isNotNull();
		assertThat(foundUser.getEmail()).isEqualTo("user@gmail.com");
	}
	
	@Test
	void findAllUsers() {
		User user2 = new User(2L, "lou@gmail.com", new Profile("Lou", "Angel Face", "https://zzz.com", false));
		
		// precondition
		given(userRepository.findAll()).willReturn(List.of(user, user2));
		
		// action
		List<User> users = userService.findAllUsers();
		
		// verify
		assertThat(users).isNotNull();
		assertThat(users).hasSize(2);
	}
	
	@Test
	void findUserByEmail() {
		// user = new User(1L, "user@gmail.com", new Profile("TylerDurden", "Space monkey", "https://zzz.com", false));
		// precondition
		given(userRepository.findFirstByEmail("user@gmail.com")).willReturn(Optional.of(user));
		
		// action
		User userFound = userService.findUserByEmail("user@gmail.com");
		
		// verify
		assertThat(userFound).isNotNull();
		assertThat(userFound.getProfile().getUsername()).isEqualTo("TylerDurden");
		verify(userRepository).findFirstByEmail("user@gmail.com");
	}
	
	@Test
	void findUserByProfileUsername() {
		// precondition
		given(userRepository.findFirstByProfileUsername("TylerDurden")).willReturn(Optional.of(user));
		
		// action
		User userFound = userService.findFirstByProfileUsername("TylerDurden");
		
		// verify
		assertThat(userFound).isNotNull();
		assertThat(userFound.getEmail()).isEqualTo("user@gmail.com");
	    verify(userRepository).findFirstByProfileUsername("TylerDurden");
	}
	
	@Test
	void updateUser() {
		// Setup
	    User existingUser = new User(1L, "user@gmail.com", new Profile("TylerDurden", "Space monkey", "https://zzz.com", false));
	    UserPutRequestDTO updatedUserDto = new UserPutRequestDTO("newname@gmail.com", "newname", "pw", "Space monkey", "https://zzz.com");

	    // Precondition
	    given(userRepository.findById(existingUser.getId())).willReturn(Optional.of(existingUser));
	    given(userRepository.existsByEmail(updatedUserDto.getEmail())).willReturn(false);
	    given(passwordEncoder.encode(updatedUserDto.getPassword())).willReturn("encodedPassword"); // dummy encoded password
	    given(userRepository.save(existingUser)).willReturn(existingUser);

	    // Action
	    User result = userService.updateUser(existingUser.getId(), updatedUserDto);

	    // Verify
	    assertThat(result).isNotNull();
	    assertThat(result.getEmail()).isEqualTo("newname@gmail.com");
	    assertThat(result.getProfile().getUsername()).isEqualTo("newname");
	    verify(userRepository).findById(existingUser.getId());
	    verify(userRepository).existsByEmail(updatedUserDto.getEmail());
	    verify(passwordEncoder).encode(updatedUserDto.getPassword());
	    verify(userRepository).save(existingUser);
	}
	
	@Test
	void deleteUser() {
		// precondition
		given(userRepository.existsById(user.getId())).willReturn(true);
		willDoNothing().given(userRepository).deleteById(user.getId());
		
		// action
		userService.deleteUser(user.getId());
		
		// verify
		verify(userRepository).existsById(user.getId());
		verify(userRepository).deleteById(user.getId());
	}
	
	@Test
	void followUserAndFindFollowersByFolloweeId() {
		User followee = new User(1L, "tyler@gmail.com", new Profile("TylerDurden", "Space monkey", "https://zzz.com", false));
	    User follower = new User(2L, "lou@gmail.com", new Profile("Lou", "Angel Face", "https://zzz.com", false));
        
		// precondition
		given(userRepository.findById(2L)).willReturn(Optional.of(follower));
		given(userRepository.findFirstByProfileUsername("TylerDurden")).willReturn(Optional.of(followee));
	    given(userRepository.save(follower)).willReturn(follower);
		
		// action
	    User result = userService.followUser(2L, "TylerDurden");
	    Set<User> followedUsers = userService.findFolloweesByFollowerId(2L);

		
		// verify
	    assertThat(result).isNotNull();
	    assertThat(followedUsers).contains(followee);
	    verify(userRepository, times(2)).findById(2L); // Once in followUser, once in findFolloweesByFollowerId
	    verify(userRepository).findFirstByProfileUsername("TylerDurden"); // Once in followUser
	    verify(userRepository).save(follower); // Once in followUser
	}
	
	@Test
	void unfollowUserAndFindFollowersByFolloweeId() {
		User followee = new User(1L, "tyler@gmail.com", new Profile("TylerDurden", "Space monkey", "https://zzz.com", false));
	    User follower = new User(2L, "lou@gmail.com", new Profile("Lou", "Angel Face", "https://zzz.com", false));

		// precondition
		given(userRepository.findById(2L)).willReturn(Optional.of(follower));
		given(userRepository.findFirstByProfileUsername("TylerDurden")).willReturn(Optional.of(followee));
	    given(userRepository.save(follower)).willReturn(follower);
		
		// action
	    User result = userService.followUser(2L, "TylerDurden");
	    result = userService.unfollowUser(2L, "TylerDurden");
	    Set<User> followedUsers = userService.findFolloweesByFollowerId(2L);
		
		// verify
		assertThat(result).isNotNull();
		assertThat(followedUsers).isEmpty();
	    verify(userRepository, times(3)).findById(2L); // followUser, unfollowUser, findFolloweesByFollowerId
		verify(userRepository, times(2)).findFirstByProfileUsername("TylerDurden"); // Once in followUser, once in unfollowUser
	    verify(userRepository, times(2)).save(follower); // followUser, unfollowUser
	}
	
	@Test
	void viewProfile() {
	    // precondition
		given(userRepository.findFirstByProfileUsername("TylerDurden")).willReturn(Optional.of(user));
	    
		// action
	    User result = userService.viewProfile("TylerDurden");
	    
	    // verify
	    assertThat(result).isNotNull();
	    assertThat(result.getProfile().getUsername()).isEqualTo("TylerDurden");
	    verify(userRepository).findFirstByProfileUsername("TylerDurden");
	}
	
	@Test
	void registerUser() {
	    // precondition
		User user2 = new User(2L, "lou@gmail.com", "password", new Profile("Lou", "Angel Face", "https://zzz.com", false));
		given(userRepository.existsByEmail(user2.getEmail())).willReturn(false);
		given(userRepository.save(user2)).willReturn(user2);
		given(passwordEncoder.encode(user2.getPassword())).willReturn("encoded");
		
		// action
	    User result = userService.register(user2);
	    
	    // verify
	    assertThat(result).isNotNull();
	    assertThat(result.getEmail()).isEqualTo(user2.getEmail());
	    verify(userRepository).existsByEmail(user2.getEmail());
	    verify(userRepository).save(user2);
	    verify(passwordEncoder).encode("password");
	}
	
	@Test
	void loginUser() {
		// precondition
		Authentication mockAuthentication = mock(Authentication.class);
//		given(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())))
//        			.willReturn(mockAuthentication);
		given(userRepository.findFirstByEmail(user.getEmail())).willReturn(Optional.of(user));
		given(passwordEncoder.matches(user.getPassword(), user.getPassword())).willReturn(true);
		
		// action
		User result = userService.login(user.getEmail(), user.getPassword());
		
	    // verify
	    assertThat(result).isNotNull();
	    assertThat(result.getId()).isEqualTo(user.getId());
	    assertThat(result.getEmail()).isEqualTo(user.getEmail());
//	    verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
	    verify(userRepository).findFirstByEmail(user.getEmail());
	    verify(passwordEncoder).matches(user.getPassword(), user.getPassword());
	}
	
	@Test
	void generateToken() {
		// precondition
		UserDetails userDetails = user;
		given(jwtService.generateToken(userDetails)).willReturn("token");
		
		// action
		String result = userService.generateToken(userDetails);
		
	    // verify
	    assertThat(result).isNotNull();
	    assertThat(result).isEqualTo("token");
	    verify(jwtService).generateToken(userDetails);
	}
}