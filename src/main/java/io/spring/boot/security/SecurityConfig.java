<<<<<<< Updated upstream
package io.spring.boot.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import io.spring.boot.repository.UserRepository;
import io.spring.boot.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
    private final UserRepository userRepository;
    
	public SecurityConfig(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())		// Disable CSRF protection since we're using stateless JWT authentication
                .authorizeHttpRequests(auth -> auth		// Configure authorization rules for HTTP requests
                        // Public endpoints (no authentication required)
                        .requestMatchers("/api/users", "/api/users/login").permitAll()
                        .requestMatchers("/api/tags").permitAll()
                        .requestMatchers("/api/articles").permitAll()
                        .requestMatchers("/api/articles/{slug}").permitAll()
                        .requestMatchers("/api/articles/{slug}/comments").permitAll()
                        .requestMatchers("/api/profiles/{username}").permitAll()
                        // Authenticated endpoints (require JWT)
                        .requestMatchers("/api/user").authenticated()
                        .requestMatchers("/api/profiles/{username}/follow").authenticated()
                        .requestMatchers("/api/articles/feed").authenticated()
                        .requestMatchers("/api/articles/**").authenticated() // Covers POST, PUT, DELETE
                        .requestMatchers("/api/articles/{slug}/favorite").authenticated()
                        .requestMatchers("/api/articles/{slug}/comments/{id}").authenticated()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/users/login", "/api/users").permitAll()
                        // Fallback: any other /api/** requires authentication
//                        .requestMatchers("/api/**").authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))	// Use stateless session management (no server-side session, relies on JWT)
                .authenticationProvider(authenticationProvider())	// Set the custom authentication provider (uses UserService and PasswordEncoder)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);	// Add JwtAuthenticationFilter before Spring's default auth filter
        return http.build();	// Build and return the security filter chain
    }

    // Provides a BCryptPasswordEncoder for hashing passwords during registration (used in UserService) and verifying them during login.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // ← Add this method to provide UserService without direct dependency
    @Bean
    public UserService userService() {
    	return new UserService(userRepository, passwordEncoder(), jwtService());
    }
    
    @Bean
    public JwtService jwtService() {
    		return new JwtService();
    }
    
    // Configures a DaoAuthenticationProvider to handle authentication by loading users (via UserService) and verifying passwords (via PasswordEncoder).
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();	// Create a DAO-based authentication provider
        authProvider.setUserDetailsService(userService());		// Set UserService to load users by email
        authProvider.setPasswordEncoder(passwordEncoder());		// Set BCrypt encoder for password verification
        return authProvider;
    }

    // Provides the AuthenticationManager to authenticate users during login by coordinating with the AuthenticationProvider.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();		// Provide the AuthenticationManager for login (used in UserController)
    }
	
}
/*
 * How the Beans Interact Here’s how the beans work together during key
 * application events:
 * 
 * Application Startup:
 * 
 * All Beans Created: Spring creates all beans (SecurityFilterChain,
 * PasswordEncoder, AuthenticationProvider, AuthenticationManager) when the
 * application starts, as they are annotated with @Bean. Dependencies Injected:
 * The SecurityFilterChain receives jwtAuthFilter and authenticationProvider,
 * and the AuthenticationProvider receives userService and passwordEncoder.
 * 
 * 
 * User Registration (POST /api/users):
 * 
 * Beans Involved:
 * 
 * PasswordEncoder: Used by UserService to hash the password.
 * SecurityFilterChain: Allows the request (due to .permitAll() for /api/users),
 * so no authentication is needed.
 * 
 * 
 * Interaction: The UserService (Step 5) calls passwordEncoder.encode to hash
 * the password, but no other beans are directly involved since registration is
 * public.
 * 
 * 
 * User Login (POST /api/users/login):
 * 
 * Beans Involved:
 * 
 * SecurityFilterChain: Permits the request (.permitAll() for /api/users/login).
 * AuthenticationManager: Called by UserController to authenticate the email and
 * password. AuthenticationProvider: Used by AuthenticationManager to load the
 * user (via UserService) and verify the password (via PasswordEncoder).
 * PasswordEncoder: Verifies the provided password against the stored hash.
 * 
 * 
 * Interaction:
 * 
 * UserController sends the email and password to
 * AuthenticationManager.authenticate. The AuthenticationManager delegates to
 * the AuthenticationProvider. The AuthenticationProvider uses UserService to
 * load the user by email (findFirstByEmail) and PasswordEncoder to check the
 * password. If valid, a JWT is generated (via JwtService in UserService), but
 * this is handled outside SecurityConfig.
 * 
 * 
 * 
 * 
 * Protected Route Access (e.g., GET /api/articles):
 * 
 * Beans Involved:
 * 
 * SecurityFilterChain: Enforces .anyRequest().authenticated(), triggering the
 * JwtAuthenticationFilter. JwtAuthenticationFilter (injected into
 * SecurityFilterChain): Validates the JWT and sets the authenticated user.
 * AuthenticationProvider: Not directly involved, as the filter validates the
 * JWT without re-checking credentials.
 * 
 * 
 * Interaction:
 * 
 * The SecurityFilterChain processes the request, invoking
 * JwtAuthenticationFilter. The filter uses JwtService to validate the JWT and
 * UserService to load the user, setting the authentication in
 * SecurityContextHolder. If the JWT is invalid or missing, the chain returns a
 * 401 Unauthorized response.
 */
=======
package io.spring.boot.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import io.spring.boot.repository.UserRepository;
import io.spring.boot.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
    private final UserRepository userRepository;
    
	public SecurityConfig(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())		// Disable CSRF protection since we're using stateless JWT authentication
                .authorizeHttpRequests(auth -> auth		// Configure authorization rules for HTTP requests
                        // Public endpoints (no authentication required)
                        .requestMatchers("/api/users", "/api/users/login").permitAll()
                        .requestMatchers("/api/tags").permitAll()
                        .requestMatchers("/api/articles").permitAll()
                        .requestMatchers("/api/articles/{slug}").permitAll()
                        .requestMatchers("/api/articles/{slug}/comments").permitAll()
                        .requestMatchers("/api/profiles/{username}").permitAll()
                        // Authenticated endpoints (require JWT)
                        .requestMatchers("/api/user").authenticated()
                        .requestMatchers("/api/profiles/{username}/follow").authenticated()
                        .requestMatchers("/api/articles/feed").authenticated()
                        .requestMatchers("/api/articles/**").authenticated() // Covers POST, PUT, DELETE
                        .requestMatchers("/api/articles/{slug}/favorite").authenticated()
                        .requestMatchers("/api/articles/{slug}/comments/{id}").authenticated()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/users/login", "/api/users").permitAll()
                        // Fallback: any other /api/** requires authentication
//                        .requestMatchers("/api/**").authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))	// Use stateless session management (no server-side session, relies on JWT)
                .authenticationProvider(authenticationProvider())	// Set the custom authentication provider (uses UserService and PasswordEncoder)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);	// Add JwtAuthenticationFilter before Spring's default auth filter
        return http.build();	// Build and return the security filter chain
    }

    // Provides a BCryptPasswordEncoder for hashing passwords during registration (used in UserService) and verifying them during login.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // ← Add this method to provide UserService without direct dependency
    @Bean
    public UserService userService() {
    	return new UserService(userRepository, passwordEncoder(), jwtService());
    }
    
    @Bean
    public JwtService jwtService() {
    		return new JwtService();
    }
    
    // Configures a DaoAuthenticationProvider to handle authentication by loading users (via UserService) and verifying passwords (via PasswordEncoder).
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();	// Create a DAO-based authentication provider
        authProvider.setUserDetailsService(userService());		// Set UserService to load users by email
        authProvider.setPasswordEncoder(passwordEncoder());		// Set BCrypt encoder for password verification
        return authProvider;
    }

    // Provides the AuthenticationManager to authenticate users during login by coordinating with the AuthenticationProvider.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();		// Provide the AuthenticationManager for login (used in UserController)
    }
	
}
/*
 * How the Beans Interact Here’s how the beans work together during key
 * application events:
 * 
 * Application Startup:
 * 
 * All Beans Created: Spring creates all beans (SecurityFilterChain,
 * PasswordEncoder, AuthenticationProvider, AuthenticationManager) when the
 * application starts, as they are annotated with @Bean. Dependencies Injected:
 * The SecurityFilterChain receives jwtAuthFilter and authenticationProvider,
 * and the AuthenticationProvider receives userService and passwordEncoder.
 * 
 * 
 * User Registration (POST /api/users):
 * 
 * Beans Involved:
 * 
 * PasswordEncoder: Used by UserService to hash the password.
 * SecurityFilterChain: Allows the request (due to .permitAll() for /api/users),
 * so no authentication is needed.
 * 
 * 
 * Interaction: The UserService (Step 5) calls passwordEncoder.encode to hash
 * the password, but no other beans are directly involved since registration is
 * public.
 * 
 * 
 * User Login (POST /api/users/login):
 * 
 * Beans Involved:
 * 
 * SecurityFilterChain: Permits the request (.permitAll() for /api/users/login).
 * AuthenticationManager: Called by UserController to authenticate the email and
 * password. AuthenticationProvider: Used by AuthenticationManager to load the
 * user (via UserService) and verify the password (via PasswordEncoder).
 * PasswordEncoder: Verifies the provided password against the stored hash.
 * 
 * 
 * Interaction:
 * 
 * UserController sends the email and password to
 * AuthenticationManager.authenticate. The AuthenticationManager delegates to
 * the AuthenticationProvider. The AuthenticationProvider uses UserService to
 * load the user by email (findFirstByEmail) and PasswordEncoder to check the
 * password. If valid, a JWT is generated (via JwtService in UserService), but
 * this is handled outside SecurityConfig.
 * 
 * 
 * 
 * 
 * Protected Route Access (e.g., GET /api/articles):
 * 
 * Beans Involved:
 * 
 * SecurityFilterChain: Enforces .anyRequest().authenticated(), triggering the
 * JwtAuthenticationFilter. JwtAuthenticationFilter (injected into
 * SecurityFilterChain): Validates the JWT and sets the authenticated user.
 * AuthenticationProvider: Not directly involved, as the filter validates the
 * JWT without re-checking credentials.
 * 
 * 
 * Interaction:
 * 
 * The SecurityFilterChain processes the request, invoking
 * JwtAuthenticationFilter. The filter uses JwtService to validate the JWT and
 * UserService to load the user, setting the authentication in
 * SecurityContextHolder. If the JWT is invalid or missing, the chain returns a
 * 401 Unauthorized response.
 */
>>>>>>> Stashed changes
