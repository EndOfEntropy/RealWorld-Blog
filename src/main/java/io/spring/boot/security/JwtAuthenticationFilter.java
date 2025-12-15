package io.spring.boot.security;


import java.io.IOException;
import java.nio.file.attribute.UserPrincipal;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.spring.boot.entity.User;
import io.spring.boot.repository.UserRepository;
import io.spring.boot.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// This class or filter is invoked for every protected routes request to check for a Token <jwt> header and authenticate the user

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    
	public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
		this.jwtService = jwtService;
		this.userService = userService;
	}

	@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");	// Get the Authorization header from the request (e.g., "Token <jwt>")

     // Check if header is missing or doesn't start with "Token " (RealWorld spec)
        if (authHeader == null || !authHeader.startsWith("Token ")) {
            // If no valid token, pass request to next filter or endpoint (e.g., for public routes)
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = authHeader.substring(6);	// Extract the JWT by removing "Token " prefix (6 characters)
        final String userEmail = jwtService.extractUsername(jwt);	// Extract the email (subject) from the JWT using JwtService

        // Proceed only if email is valid and no user is already authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userService.loadUserByUsername(userEmail); // Load UserDetails (our User entity) by email using UserService

            // Validate the JWT (checks signature and expiration) using JwtService
            if (jwtService.isTokenValid(jwt, userDetails)) {
            	
            	UserDetails fullUser = this.userService.findWithFollowedUsersByEmail(userEmail);
            	
                // Create an authentication token with user details, no credentials (JWT-based), and authorities
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                		fullUser, null, fullUser.getAuthorities());
                // Add request metadata (e.g., IP, session) to the token for auditing
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Set the authenticated user in Spring Security's context for use in controllers/services
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Pass the request to the next filter or endpoint, now with authenticated user (if valid)
        filterChain.doFilter(request, response);
    }
	
}