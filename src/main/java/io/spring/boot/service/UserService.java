package io.spring.boot.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.spring.boot.dto.UserPutRequestDTO;
import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;
import io.spring.boot.repository.UserRepository;
import io.spring.boot.security.JwtService;

@Service
public class UserService implements UserDetailsService {

	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private JwtService jwtService;
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional(readOnly = true)
	public User findUserById(Long id){
		return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("No existing user with given id: " + id));
	}
	
	@Transactional(readOnly = true)
	public List<User>findAllUsers(){
		return userRepository.findAll();
	}
	
	@Transactional(readOnly = true)
	public User findUserByEmail(String email){  
		if (email == null || email.trim().isEmpty()) {
	        throw new IllegalArgumentException("Email cannot be null or empty");
	    }
		
		return userRepository.findFirstByEmail(email)
				.orElseThrow(() -> new NoSuchElementException("No existing user with given email: " + email));
	}
	
	@Transactional(readOnly = true)
	public User findFirstByProfileUsername(String username){
		if (username == null || username.trim().isEmpty()) {
	        throw new IllegalArgumentException("Username cannot be null or empty");
	    }
		return userRepository.findFirstByProfileUsername(username)
				.orElseThrow(() -> new NoSuchElementException("No existing user with given username: " + username));
	}
	
	@Transactional
	public User saveUser(User user){
		if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
	        throw new IllegalArgumentException("Email cannot be null or empty");
	    }
		
		if (user.getProfile() == null || user.getProfile().getUsername() == null || user.getProfile().getUsername().trim().isEmpty()) {
	        throw new IllegalArgumentException("Profile username cannot be null or empty");
	    }
		
		if (userRepository.existsByEmail(user.getEmail())) {
	        throw new IllegalArgumentException("Email has already been taken");
	    }
	    if (userRepository.existsByProfileUsername(user.getProfile().getUsername())) {
	        throw new IllegalArgumentException("Username has already been taken");
	    }
		
		return userRepository.save(user);
	}
	
	@Transactional
	public User updateUser(Long id, UserPutRequestDTO dto) {
		User existingUser = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("No existing user with given id: " + id));
		
		// Business logic validation
	    if (dto.getEmail() != null && !dto.getEmail().equals(existingUser.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
	        throw new IllegalArgumentException("Email has already been taken");
	    }
	    if (dto.getUsername() != null && !dto.getUsername().equals(existingUser.getProfile().getUsername()) && 
	        userRepository.existsByProfileUsername(dto.getUsername())) {
	        throw new IllegalArgumentException("Username has already been taken");
	    }

	    // Update fields
	    if (dto.getEmail() != null) {
	        existingUser.setEmail(dto.getEmail());
	    }
	    if (dto.getPassword() != null) {
	        existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
	    }
	    if (dto.getUsername() != null || dto.getBio() != null || dto.getImage() != null) {
	        Profile profile = existingUser.getProfile();
	        if (dto.getUsername() != null) {
	            profile.setUsername(dto.getUsername());
	        }
	        if (dto.getBio() != null) {
	            profile.setBio(dto.getBio());
	        }
	        if (dto.getImage() != null) {
	            profile.setImage(dto.getImage());
	        }
	        existingUser.setProfile(profile);
	    }
	    return userRepository.save(existingUser);
	}
	
	@Transactional
	public void deleteUser(Long id) {
		if(!userRepository.existsById(id)) {
			throw new NoSuchElementException("No existing user with given id: " + id);
		}
		userRepository.deleteById(id);
	}

    @Transactional(readOnly = true)
    public User viewProfile(String username) {
    	if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
    	
        return userRepository.findFirstByProfileUsername(username).orElseThrow(
        									() -> new NoSuchElementException("User not found: " + username));
    }
	
	@Transactional
	public User followUser(Long id, String username) {
		User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));
	    User followee = userRepository.findFirstByProfileUsername(username)
	    		.orElseThrow(() -> new NoSuchElementException("User not found: " + username));
		
	    if(user.getFollowedUsers().contains(followee)){
	    	throw new IllegalArgumentException("User is already following this followee");
	    }
	    
	    user.followUser(followee);
	    userRepository.save(user);
	    return followee;
	}
	
	@Transactional
	public User unfollowUser(Long id, String username) {
		User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));
	    User followee = userRepository.findFirstByProfileUsername(username)
	    		.orElseThrow(() -> new NoSuchElementException("User not found: " + username));
	    
	    if(!user.getFollowedUsers().contains(followee)){
	    	throw new IllegalArgumentException("User is not following this followee");
	    }
	    
	    user.unfollowUser(followee);
	    userRepository.save(user);
	    return followee;
	}
	
	@Transactional(readOnly = true)
	public Set<User> findFolloweesByFollowerId(Long id){
		User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("No existing user with given id: " + id));
		
		return user.getFollowedUsers();
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		return userRepository.findFirstByEmail(email).orElseThrow(() -> new NoSuchElementException("User not found:" + email));
	}
	
	public UserDetails findWithFollowedUsersByEmail(String email){
		return userRepository.findWithFollowedUsersByEmail(email).orElseThrow(() -> new NoSuchElementException("User not found:" + email));
	}
	
	@Transactional
	public User register(User user) {
		if(userRepository.existsByEmail(user.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}
		if (userRepository.existsByProfileUsername(user.getProfile().getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}
	
	public String generateToken(UserDetails userDetails) {
		return jwtService.generateToken(userDetails);
	}
	
	@Transactional(readOnly = true)
	public User login(String email, String password) {
		User user = (User) loadUserByUsername(email);
	    if (!passwordEncoder.matches(password, user.getPassword())) {
	        throw new AuthenticationException("Invalid email or password") {};
	    }
	    return user;
    }
//	
//	@Transactional(readOnly = true)
//	public User login(String email, String password) {
//		User user = (User) loadUserByUsername(email);
//	    if (!passwordEncoder.matches(password, user.getPassword())) {
//	        throw new AuthenticationException("Invalid email or password") {};
//	    }
//	    return user;
//    }
}

