package io.spring.boot.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.spring.boot.controller.wrappers.SingleUserRequest;
import io.spring.boot.dto.UserLoginRequestDTO;
import io.spring.boot.dto.UserPostRequestDTO;
import io.spring.boot.dto.UserPutRequestDTO;
import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;
import io.spring.boot.repository.UserRepository;
import io.spring.boot.security.JwtService;
import io.spring.boot.security.SecurityConfig;
import io.spring.boot.service.UserService;

/** 
 * https://medium.com/@Lakshitha_Fernando/spring-boot-unit-testing-for-repositories-controllers-and-services-using-junit-5-and-mockito-def3ff5891be
 */
@WebMvcTest(UserRestController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class})
public class UserControllerUnitTest {

	// This annotation is used to test Spring MVC controllers. It focuses on the web layer, not loading the complete Spring application context
	@Autowired
	private MockMvc mockMvc;
	// ObjectMapper class is used for converting Java objects to JSON and vice versa.
	@Autowired
	private ObjectMapper objectMapper;
	
	// Controller calls mocked methods → returns controlled test data instead of DB calls
	@MockBean
	private UserRepository userRepository;
	@MockBean
	private UserService userService;
	// Used for token generation in controller responses (e.g., login/register).
	@MockBean
    private JwtService jwtService;
	
	private User user;
	
	@BeforeEach
	void setup() {
		user = new User(1L, "mayor@gmail.com", "password", new Profile("TylerDurden", "Space monkey", "https://zzz.com", false));
	}
	
	@Test
	public void registerTest() throws Exception {
		//precondition
		UserPostRequestDTO dto = new UserPostRequestDTO(user.getEmail(), user.getProfile().getUsername(), user.getPassword());
		given(userService.register(any(User.class))).willReturn(user);
		given(userService.generateToken(user)).willReturn("jwt.token.here");
		
		//action
		ResultActions response = mockMvc.perform(post("/api/users")
									.contentType(MediaType.APPLICATION_JSON)														//specifies the payload format
									.content(objectMapper.writeValueAsString((new SingleUserRequest<UserPostRequestDTO>(dto)))));	//provides the JSON data for the @RequestBody
		
		//verify - LocalDate.class fails for the publishDate assertion, however converting LocalDate.class to string passes
		response.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.user.email", is(user.getEmail())))
            .andExpect(jsonPath("$.user.username", is(user.getProfile().getUsername())))
            .andExpect(jsonPath("$.user.token", is("jwt.token.here")));
	}
	
	@Test
	public void loginTest() throws Exception {
		//precondition
		UserLoginRequestDTO loginDto = new UserLoginRequestDTO(user.getEmail(), user.getPassword());
		given(userService.login(any(String.class), any(String.class))).willReturn(user);
		given(userService.generateToken(user)).willReturn("jwt.token.here");
		
		//action
		ResultActions response = mockMvc.perform(post("/api/users/login")
									.contentType(MediaType.APPLICATION_JSON)															//specifies the payload format
									.content(objectMapper.writeValueAsString((new SingleUserRequest<UserLoginRequestDTO>(loginDto)))));	//provides the JSON data for the @RequestBody
		
		//verify - LocalDate.class fails for the publishDate assertion, however converting LocalDate.class to string passes
		response.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.email", is(user.getEmail())))
            .andExpect(jsonPath("$.user.username", is(user.getProfile().getUsername())))
            .andExpect(jsonPath("$.user.token", is("jwt.token.here")));
	}
	
	@Test
	public void getUsersTest() throws Exception {
		//precondition
		List<User> users = new ArrayList<User>();
		users.add(user);
		users.add(new User(2L, "mayor@gmail.com", new Profile("The Mayor", "The Mayor of Jovalisko", "https://zzz.com", false)));
		
		given(userService.findAllUsers()).willReturn(users);
		
		//action
		ResultActions response = mockMvc.perform(get("/api/users"));
		
		//verify
		response.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.size()", is(users.size())));
	}
	
	@Test
	public void getCurrentUserTest() throws Exception {
        // precondition
		given(userService.generateToken(user)).willReturn("jwt.token.here");
		
		// action
		ResultActions response = mockMvc.perform(get("/api/user").with(user(user)));
		
        // verify
        response.andDo(print())
		        .andExpect(status().isOk())
		        .andExpect(jsonPath("$.user.email", is(user.getEmail())))
		        .andExpect(jsonPath("$.user.username", is(user.getProfile().getUsername())))
		        .andExpect(jsonPath("$.user.token", is("jwt.token.here")));
	}
	
	@Test
	void updateUserTest() throws Exception {
		UserPutRequestDTO dto = new UserPutRequestDTO("mayor@gmail.com", "TheMayor", null, "Updated bio", null);

        User updatedUser = new User(user.getId(), dto.getEmail(), new Profile(dto.getUsername(), dto.getBio(), dto.getImage()));
        given(userService.updateUser(eq(user.getId()), any(UserPutRequestDTO.class))).willReturn(updatedUser);
        given(userService.generateToken(updatedUser)).willReturn("jwt.token.here");

        ResultActions response = mockMvc.perform(put("/api/user")
						        		.with(user(user))
						                .contentType(MediaType.APPLICATION_JSON)
						                .content(objectMapper.writeValueAsString((new SingleUserRequest<UserPutRequestDTO>(dto)))));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email", is(updatedUser.getEmail())))
                .andExpect(jsonPath("$.user.username", is(updatedUser.getProfile().getUsername())))
                .andExpect(jsonPath("$.user.token", is("jwt.token.here")));
	}
	
	@Test
	void deleteUserTest() throws Exception {
		// precondition
		willDoNothing().given(userService).deleteUser(user.getId());
		
		// action
		ResultActions response = mockMvc.perform(delete("/api/user")
				.with(user(user)))
		        .andExpect(status().isNoContent());
		
		// verify
		response.andDo(print()).andExpect(status().isNoContent());
	}
	
	@Test
	void viewProfile() throws Exception {
		// precondition
		User viewer = new User(2L, "viewer@gmail.com", new Profile("viewer"));
		given(userService.viewProfile("TylerDurden")).willReturn(user);
		
		// action
		ResultActions response = mockMvc.perform(get("/api/profiles/{username}", "TylerDurden").with(user(viewer)));
		
		// verify
		response.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.profile.username", is(user.getProfile().getUsername())))
			.andExpect(jsonPath("$.profile.following", is(false)));
	}
	
	@Test
	void followUser() throws Exception {
		// precondition
		User follower = new User(2L, "follower@gmail.com", new Profile("follower"));
		follower.followUser(user);
		given(userService.followUser(follower.getId(), "TylerDurden")).willReturn(user);
		
		// action
        ResultActions response = mockMvc.perform(post("/api/profiles/{username}/follow", "TylerDurden").with(user(follower)));

		// verify
		response.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.profile.username").value("TylerDurden"))
			.andExpect(jsonPath("$.profile.following").value(true));
	}
	
	@Test
	void unfollowUser() throws Exception {
		// precondition
		User follower = new User(2L, "follower@gmail.com", new Profile("follower"));
		follower.followUser(user);
		follower.unfollowUser(user);
		given(userService.unfollowUser(follower.getId(), "TylerDurden")).willReturn(user);
		
		// action
        ResultActions response = mockMvc.perform(delete("/api/profiles/{username}/follow", "TylerDurden").with(user(follower)));
		
		// verify
		response.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.profile.username").value("TylerDurden"))
			.andExpect(jsonPath("$.profile.following").value(false));
	}
}
