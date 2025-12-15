package io.spring.boot.service;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;

//FOR REFERENCE ONLY - Used ins this Github implementation
//https://github.com/raeperd/realworld-springboot-java/blob/master/src/main/java/io/github/raeperd/realworld/domain/user/ProfileService.java

//@Service  // Marks this class as a Spring service, making it a candidate for auto-detection and dependency injection.
public class ProfileService {

    private final UserFindService userFindService;  // Declares a dependency on UserFindService, which will be injected by Spring.

    // Constructor that injects the UserFindService dependency into the ProfileService.
    @Autowired
    public ProfileService(UserFindService userFindService) {
        this.userFindService = userFindService;
    }
    
	/*
	 * Fetches the viewer user by their ID, throws NoSuchElementException if not
	 * found.
	 * 
	 * Attempts to find the user to view by username. If found, it calls viewer.viewProfile() and returns the result.
	 * If not found, it throws NoSuchElementException.
	 * 
	 * Profile Profile.withFollowing(boolean following) assigns value to
	 * this.following and returns Profile
	 */
    
    // The @Transactional annotation makes the method's operation part of a database transaction.
    // readOnly = true optimizes for read-only operations to prevent accidental writes.
    @Transactional(readOnly = true)
    public Profile viewProfile(Long userId, String usernameToView) {
        // Fetches the viewer user by their ID, throws NoSuchElementException if not found.
        final User viewer = userFindService.findById(userId).orElseThrow(NoSuchElementException::new);
        
        // Attempts to find the user by username. If found, it calls viewer.viewProfile() and returns the result.
        // If not found, it throws NoSuchElementException.
        return userFindService.findByUsername(usernameToView)
                .map(viewer::viewProfile)  // If user is found, call viewer's viewProfile method
                .orElseThrow(NoSuchElementException::new);  // If username not found, throw exception
	//		this code is equivalent to the above
	//		final User viewer = userFindService.findById(viewerId).orElseThrow(NoSuchElementException::new);
	//		User sameViewer = userFindService.findByUsername(usernameToView).orElseThrow(NoSuchElementException::new);
	//		return sameViewer.viewProfile(sameViewer);
    }
    
    // Another version of viewProfile, allowing profile viewing by username only.
    // Returns the profile associated with the given username.
    @Transactional(readOnly = true)
    public Profile viewProfile(String username) {
        // Tries to find the user by username and return their profile.
        // If user not found, throws NoSuchElementException.
        return userFindService.findByUsername(username)
                .map(User::getProfile)  // If user is found, return their profile.
                .orElseThrow(() -> new NoSuchElementException());  // If user not found, throw exception.
	//		this code is equivalent to the above
	//      User viewer = userFindService.findByUsername(username).orElseThrow(NoSuchElementException::new);
	//		return viewer.getProfile();
    }

    @Transactional
    public Profile followAndViewProfile(Long followerId, String followeeUserName) {
        // Fetch the followee by username, throwing an exception if not found
        final var followee = userFindService.findByUsername(followeeUserName)
                .orElseThrow(NoSuchElementException::new);

        // Find the follower by ID, and apply transformations using Optional's map
        // Perform the follow action which returns the modified follower to be passed to the next map
        // Use the modified follower to view the profile of the followee
        return userFindService.findById(followerId)
                .map(follower -> follower.followUser(followee))
                .map(follower -> follower.viewProfile(followee))
                .orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    public Profile unfollowAndViewProfile(Long followerId, String followeeUserName) {
        // Fetch the followee by username, throwing an exception if not found
        final var followee = userFindService.findByUsername(followeeUserName)
                .orElseThrow(NoSuchElementException::new);

        // Find the follower by ID, and apply transformations using Optional's map
        // Perform the unfollow action which returns the modified follower to be passed to the next map
        // Use the modified follower to view the profile of the followee
        return userFindService.findById(followerId)
                .map(follower -> follower.unfollowUser(followee))
                .map(follower -> follower.viewProfile(followee))
                .orElseThrow(NoSuchElementException::new);
    }
}
