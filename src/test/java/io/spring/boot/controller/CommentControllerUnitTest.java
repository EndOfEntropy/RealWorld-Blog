package io.spring.boot.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.Set;

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

import io.spring.boot.controller.wrappers.SingleCommentPostRequest;
import io.spring.boot.dto.CommentPostRequestDTO;
import io.spring.boot.entity.Article;
import io.spring.boot.entity.Comment;
import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;
import io.spring.boot.repository.UserRepository;
import io.spring.boot.security.JwtService;
import io.spring.boot.security.SecurityConfig;
import io.spring.boot.service.CommentService;
import io.spring.boot.service.UserService;

@WebMvcTest(CommentRestController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class})
public class CommentControllerUnitTest {
	
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	// Controller calls mocked methods → returns controlled test data instead of DB calls
	@MockBean
	private UserService userService;
	// Used for token generation in controller responses (e.g., login/register).
	@MockBean
    private JwtService jwtService;
	@MockBean
	private CommentService commentService;
	@MockBean
	private UserRepository userRepository;
	
	
	private Article article;
	private User author;
	private Comment comment;
	
	@BeforeEach
	void setup() {
        author = new User(1L, "user@gmail.com", new Profile("username"));
		article = new Article(1L, "title", "desc", "body");
		article.setAuthor(author);
		
		comment = new Comment(1L, "comment");
		comment.setArticle(article);
		comment.setAuthor(author);
	}
	
	@Test
	public void saveCommentTest() throws Exception {
		Comment savedComment = new Comment(2L, "comment2");
	    savedComment.setArticle(article);
	    savedComment.setAuthor(author);
		SingleCommentPostRequest payload = new SingleCommentPostRequest(new CommentPostRequestDTO(savedComment.getBody()));
		given(commentService.createComment(any(User.class), anyString(), anyString())).willReturn(savedComment);
		
		ResultActions result = mockMvc.perform(post("/api/articles/{slug}/comments", "title2")
								.with(user(author))
								.contentType(MediaType.APPLICATION_JSON)				//specifies the payload format
								.content(objectMapper.writeValueAsString(payload)));	//provides the JSON data for the @RequestBody
		
		result.andDo(print())
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.comment.id", is(2)))
					.andExpect(jsonPath("$.comment.body", is("comment2")));
	}
	
	@Test
	public void findAllCommentsTest() throws Exception {
		given(commentService.findAllComments(anyString())).willReturn(Set.of(comment));

        ResultActions result = mockMvc.perform(get("/api/articles/{slug}/comments", article.getSlug())
        							.with(user(author)));

        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments[0].body", is("comment")))
                .andExpect(jsonPath("$.comments[0].author.username", is("username")));
	}
	
	@Test
	public void deleteCommentTest() throws Exception {
		given(commentService.findById(comment.getId())).willReturn(Optional.of(comment));
		willDoNothing().given(commentService).deleteCommentById(any(Long.class));
		
        ResultActions result = mockMvc.perform(delete("/api/articles/{slug}/comments/{id}", article.getSlug(), comment.getId())
        							.with(user(author)));

        result.andDo(print()).andExpect(status().isNoContent());
	}
	
}
