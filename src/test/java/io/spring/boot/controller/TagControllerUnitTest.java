package io.spring.boot.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import io.spring.boot.entity.Tag;
import io.spring.boot.repository.UserRepository;
import io.spring.boot.security.JwtAuthenticationFilter;
import io.spring.boot.security.JwtService;
import io.spring.boot.security.SecurityConfig;
import io.spring.boot.service.TagService;
import io.spring.boot.service.UserService;

@WebMvcTest(TagRestController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class})
public class TagControllerUnitTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private TagService tagService;
	@MockBean
    private JwtService jwtService;
	@MockBean
    private UserService userService;
	@MockBean
	private UserRepository userRepository;
	
	private List<Tag> tagList;
	
	@BeforeEach
	void setup() {
		tagList = List.of(new Tag("react"), new Tag("angular"), new Tag("vue")); //"react", "angular", "vue"
	}
	
	@Test
	public void findAllTagsTest() throws Exception {
		given(tagService.findAllTags()).willReturn(tagList);
		
		ResultActions response = mockMvc.perform(get("/api/tags")
                .contentType(MediaType.APPLICATION_JSON));

		response.andDo(print())
		        .andExpect(status().isOk())
		        .andExpect(jsonPath("$.tags.size()", is(3)))
		        .andExpect(jsonPath("$.tags[0]", is("react")))
		        .andExpect(jsonPath("$.tags[1]", is("angular")))
		        .andExpect(jsonPath("$.tags[2]", is("vue")));
	}
}
