package io.spring.boot.controller;


import java.util.List;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.spring.boot.controller.wrappers.SingleProfileResponse;
import io.spring.boot.controller.wrappers.SingleUserRequest;
import io.spring.boot.controller.wrappers.SingleUserResponse;
import io.spring.boot.dto.AuthorResponseDTO;
import io.spring.boot.dto.UserLoginRequestDTO;
import io.spring.boot.dto.UserPostRequestDTO;
import io.spring.boot.dto.UserPutRequestDTO;
import io.spring.boot.dto.UserResponseDTO;
import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;
import io.spring.boot.service.UserService;
import jakarta.validation.Valid;

/**
 * This unit test injects the user service dependency to send responses to the controller
 */
@RestController
@RequestMapping("/api")
class UserRestController {

	private final UserService userService;

	@Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }
	
	@PostMapping("/users")
	public ResponseEntity<SingleUserResponse> register(@Valid @RequestBody SingleUserRequest<UserPostRequestDTO> request){
		User user = new User(request.user().getEmail(), request.user().getPassword(), new Profile(request.user().getUsername()));
		User savedUser = userService.register(user);
        UserResponseDTO response = new UserResponseDTO(savedUser, userService.generateToken(savedUser));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new SingleUserResponse(response));
	}
	
	@PostMapping("/users/login")
	public ResponseEntity<SingleUserResponse> login(@Valid @RequestBody SingleUserRequest<UserLoginRequestDTO> request) throws AuthenticationException{
		User user = userService.login(request.user().getEmail(), request.user().getPassword());
        UserResponseDTO response = new UserResponseDTO(user, userService.generateToken(user));
		return ResponseEntity.ok(new SingleUserResponse(response));
	}
	
	@GetMapping("/user")
	public ResponseEntity<SingleUserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
		if(user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
		}
		UserResponseDTO response = new UserResponseDTO(user, userService.generateToken(user));
        return ResponseEntity.ok(new SingleUserResponse(response));
	}
	
	@PutMapping("/user")
	public ResponseEntity<SingleUserResponse> updateUser(@AuthenticationPrincipal User user, 
															@Valid @RequestBody SingleUserRequest<UserPutRequestDTO> request){
		if(user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
		}
		User updatedUser = userService.updateUser(user.getId(), request.user());
	    UserResponseDTO response = new UserResponseDTO(
	        updatedUser.getEmail(),
	        userService.generateToken(updatedUser),
	        updatedUser.getProfile().getUsername(),
	        updatedUser.getProfile().getBio(),
	        updatedUser.getProfile().getImage()
	    );
	    return ResponseEntity.ok(new SingleUserResponse(response));
	}
	
	@GetMapping("/profiles/{username}")
	public ResponseEntity<SingleProfileResponse> viewProfile(@AuthenticationPrincipal User viewer, @PathVariable String username){
		User targetUser = userService.viewProfile(username);
		boolean following = viewer != null && viewer.getFollowedUsers().stream().anyMatch(u -> u.getId().equals(targetUser.getId()));
		AuthorResponseDTO dto = new AuthorResponseDTO(targetUser.getProfile(), following);
		
		return ResponseEntity.ok(new SingleProfileResponse(dto));
	}
	
	@PostMapping("/profiles/{username}/follow")
	public ResponseEntity<SingleProfileResponse> followUser(@AuthenticationPrincipal User currentUser, @PathVariable String username){
		if(currentUser == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
		}
		User user = userService.followUser(currentUser.getId(), username);
		boolean following = currentUser != null && currentUser.getFollowedUsers().stream().anyMatch(u -> u.getId().equals(user.getId()));
		AuthorResponseDTO dto = new AuthorResponseDTO(user.getProfile(), following);
		
		return ResponseEntity.ok(new SingleProfileResponse(dto));
	}
	
	@DeleteMapping("/profiles/{username}/follow")
	public ResponseEntity<SingleProfileResponse> unfollowUser(@AuthenticationPrincipal User currentUser, @PathVariable String username){
		if(currentUser == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
		}
		User user = userService.unfollowUser(currentUser.getId(), username);
		boolean following = currentUser != null && currentUser.getFollowedUsers().stream().anyMatch(u -> u.getId().equals(user.getId()));
		AuthorResponseDTO dto = new AuthorResponseDTO(user.getProfile(), following);
		
		return ResponseEntity.ok(new SingleProfileResponse(dto));
	}
	
	@GetMapping("/users")
	public ResponseEntity<List<User>>findAll(){
		return ResponseEntity.ok(userService.findAllUsers());
	}

	@DeleteMapping("/user")
	public ResponseEntity<Void>deleteUser(@AuthenticationPrincipal User user){
		if(user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
		}
		userService.deleteUser(user.getId());
		return ResponseEntity.noContent().build();
	}
	
}