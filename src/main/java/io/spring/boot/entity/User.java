/**
 * 
 */
package io.spring.boot.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * 
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "email", unique = true, nullable = false)
	private String email;
	
	@Column(name = "password", nullable = false)
	private String password;

	@Embedded
	private Profile profile;
	
	// represents the set of users that a given user (the follower) is following (the followees).
	@ManyToMany
	@JoinTable(
			name = "user_followings",
			joinColumns = @JoinColumn(name = "follower_id"),
			inverseJoinColumns = @JoinColumn(name = "followee_id"))
	private Set<User> followedUsers = new HashSet<>();
	
	// represents the set of users that a given user (the followee) is followed by (the followers).
	// mappedBy = "followedUsers" indicates that the followedUsers set owns the relationship, 
	// and followeeUsers is the inverse side. JPA synchronizes both sides based on the user_followings table.
	//	@ManyToMany(mappedBy = "followingUsers")
	//	private Set<User> followeeUsers = new HashSet<>();

	@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
	Set<Article> user_articles = new HashSet<Article>();
	
	@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
	Set<Comment> user_comments = new HashSet<Comment>();
	
	// JPA no-arg constructor
	protected User() {
	}

	public User(Long id, String email, String password, Profile profile) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.profile = profile;
	}
	// for testing purposes only
	public User(Long id, String email, Profile profile) {
		this.id = id;
		this.email = email;
		this.password = "";
		this.profile = profile;
	}

	public User(String email, String password, Profile profile) {
		this.email = email;
		this.password = password;
		this.profile = profile;
	}
	// for testing purposes only
	public User(String email, Profile profile) {
		this.email = email;
		this.password = "";
		this.profile = profile;
	}
	
	public User followUser(User followee) {
		followedUsers.add(followee);
		return this;
	}
	
	public User unfollowUser(User followee) {
		followedUsers.remove(followee);
		return this;
	}
	
	public Profile viewProfile(User user) {
		return user.getProfile().withFollowing(followedUsers.contains(user));
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public Set<User> getFollowedUsers() {
		return followedUsers;
	}

	public void setFollowedUsers(Set<User> followedUsers) {
		this.followedUsers = followedUsers;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", email=" + email + ", profile=" + profile + ", followingUsers=" + followedUsers
				+ "]";
	}
	
	// Implement UserDetails methods
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;	// Use email as username for authentication
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
