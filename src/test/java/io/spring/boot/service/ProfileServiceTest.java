package io.spring.boot.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProfileServiceTest {

	@Mock
	private UserFindService userFindService;
	
	@InjectMocks
	private ProfileService profileService;
	
	// No setup method needed as @InjectMocks automatically handles this
	
	@Test
	@Order(1)
	void when_viewProfile_with_viewer_not_exists_expect_NoSuchElementException() {
        // Simulate that userFindService returns an empty Optional, i.e., user id not found.
        when(userFindService.findById(1L)).thenReturn(Optional.empty());
				
        // Assert that calling viewProfile will throw a NoSuchElementException
        assertThatThrownBy(() -> profileService.viewProfile(1L, anyString())).isInstanceOf(NoSuchElementException.class);
	}

	/*
	 * The part (@Mock User user) is a Mockito annotation that is used to mock the User object for the test.
	 * It is necessary for simulating the presence of a User object without needing to create a real instance of it.
	 */
	@Test
	@Order(2)
	void when_viewProfile_with_username_not_exists_expect_NoSuchElementException(@Mock User user) {
		// Simulate that userFindService returns a non-empty Optional, i.e., a valid user is found by ID.
		when(userFindService.findById(anyLong())).thenReturn(Optional.of(user));
		
        // Simulate that userFindService returns an empty Optional, i.e., user name not found.
        when(userFindService.findByUsername(anyString())).thenReturn(Optional.empty());
				
        // Assert that calling viewProfile will throw a NoSuchElementException
        assertThatThrownBy(() -> profileService.viewProfile(1L, anyString())).isInstanceOf(NoSuchElementException.class);
	}
	
	@Test
	@Order(3)
	void when_viewProfile_expect_viewer_view_found_user(@Mock User viewer, @Mock User userToView, @Mock Profile profileToView) {
		// Simulate that userFindService returns a non-empty Optional, i.e., a valid user is found by ID.
		given(userFindService.findById(anyLong())).willReturn(Optional.of(viewer));
		
        // Simulate that userFindService returns an empty Optional, i.e., a valid user is found by user name.
		given(userFindService.findByUsername(anyString())).willReturn(Optional.of(userToView));
        
        // Simulate the behavior of the viewer (user) calling the 'viewProfile' method on the user they want to view.
        // The viewer is supposed to call 'viewProfile' and return the profile of the user they want to view.
		given(viewer.viewProfile(userToView)).willReturn(profileToView);
        
        // Execute the 'viewProfile' method of the ProfileService with valid parameters (viewer and userToView).
        profileService.viewProfile(1L, anyString());
        
        // Verify that the 'viewProfile' method of the viewer was called exactly once with the user to view as the argument.
        then(viewer).should(times(1)).viewProfile(userToView);
	}
	
    @Test
    @Order(4)
    void when_viewProfile_with_not_exists_username_expect_NoSuchElementException() {
        // Simulate that userFindService returns an empty Optional, i.e., user name not found.
        when(userFindService.findByUsername(anyString())).thenReturn(Optional.empty());
				
        // Assert that calling viewProfile will throw a NoSuchElementException
        assertThatThrownBy(() -> profileService.viewProfile(anyString())).isInstanceOf(NoSuchElementException.class);
    }
    
    @Test
    @Order(5)
    void when_viewProfile_expect_user_getProfile(@Mock User userToView, @Mock Profile profileToView) {
        // Simulate that the userFindService returns a valid user when looking up by username.
        given(userFindService.findByUsername(anyString())).willReturn(Optional.of(userToView));

        // Here, we mock the 'getProfile' method to return a predefined mock 'profileToView'.
        given(userToView.getProfile()).willReturn(profileToView);
        
        // Execute the 'viewProfile' method of the ProfileService. This will trigger the mocked behaviors defined above.
        profileService.viewProfile(anyString());
        
        // This ensures that the ProfileService correctly calls the method to get the profile of the user to view.
        then(userToView).should(times(1)).getProfile();
    }
    
    @Test
    @Order(6)
    void when_followAndViewProfile_with_not_exists_followeeName_expect_NoSuchElementException() {
        
        // Simulate that userFindService returns an empty Optional, i.e., user name not found.
        when(userFindService.findByUsername(anyString())).thenReturn(Optional.empty());
        
        // Assert that calling followAndViewProfile will throw a NoSuchElementException
        assertThatThrownBy(() -> profileService.followAndViewProfile(1L, anyString()))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @Order(7)
    void when_followAndViewProfile_with_not_exists_followeeId_expect_NoSuchElementException(@Mock User followee) {
        
        // Simulate that userFindService returns an empty Optional, i.e., user ID not found.
        when(userFindService.findById(anyLong())).thenReturn(Optional.empty());
        
        // Simulate that userFindService returns the followee by username
        when(userFindService.findByUsername(anyString())).thenReturn(Optional.of(followee));
        
        // Assert that calling followAndViewProfile will throw a NoSuchElementException
        assertThatThrownBy(() -> profileService.followAndViewProfile(1L, anyString()))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @Order(8)
    void when_followAndViewProfile_expect_follower_follows_followee(@Mock User follower, @Mock User followee, @Mock Profile followeeProfile) {
        // Simulate that userFindService returns the follower by ID (returns the mock follower object)
        when(userFindService.findById(anyLong())).thenReturn(Optional.of(follower));
        
        // Simulate that userFindService returns the followee by username (returns the mock followee object)
        when(userFindService.findByUsername(anyString())).thenReturn(Optional.of(followee));
        
        // Simulate that when the follower calls followUser(followee), it returns the follower object itself
        given(follower.followUser(followee)).willReturn(follower);
        
        // Simulate that when the follower views the profile of the followee, it returns the mock profile
        given(follower.viewProfile(followee)).willReturn(followeeProfile);
        
        // Call the method being tested, which should trigger the follower to follow and view the followee's profile
        profileService.followAndViewProfile(1L, anyString());
        
        // Verify that the followUser method was called exactly once on the follower with the followee as the argument
        then(follower).should(times(1)).followUser(followee);
    }

    @Test
    @Order(9)
    void when_unfollowAndViewProfile_with_not_exists_followeeName_expect_NoSuchElementException() {
        
        // Simulate that userFindService returns an empty Optional, i.e., user name not found.
        when(userFindService.findByUsername(anyString())).thenReturn(Optional.empty());
        
        // Assert that calling unfollowAndViewProfile will throw a NoSuchElementException
        assertThatThrownBy(() -> profileService.unfollowAndViewProfile(1L, anyString()))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @Order(10)
    void when_unfollowAndViewProfile_with_not_exists_followeeId_expect_NoSuchElementException(@Mock User followee) {
        
        // Simulate that userFindService returns an empty Optional, i.e., user ID not found.
        when(userFindService.findById(anyLong())).thenReturn(Optional.empty());
        
        // Simulate that userFindService returns the followee by username
        when(userFindService.findByUsername(anyString())).thenReturn(Optional.of(followee));
        
        // Assert that calling unfollowAndViewProfile will throw a NoSuchElementException
        assertThatThrownBy(() -> profileService.unfollowAndViewProfile(1L, anyString()))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @Order(11)
    void when_unfollowAndViewProfile_expect_follower_unfollows_followee(@Mock User follower, @Mock User followee, @Mock Profile followeeProfile) {
        // Simulate that userFindService returns the follower by ID (returns the mock follower object)
        when(userFindService.findById(anyLong())).thenReturn(Optional.of(follower));
        
        // Simulate that userFindService returns the followee by username (returns the mock followee object)
        when(userFindService.findByUsername(anyString())).thenReturn(Optional.of(followee));
        
        // Simulate that when the follower calls unfollowUser(followee), it returns the follower object itself
        given(follower.unfollowUser(followee)).willReturn(follower);
        
        // Simulate that when the follower views the profile of the followee, it returns the mock profile
        given(follower.viewProfile(followee)).willReturn(followeeProfile);
        
        // Call the method being tested, which should trigger the follower to unfollow and view the followee's profile
        profileService.unfollowAndViewProfile(1L, anyString());
        
        // Verify that the unfollowUser method was called exactly once on the follower with the followee as the argument
        then(follower).should(times(1)).unfollowUser(followee);
    }

}