package io.spring.boot.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.spring.boot.controller.wrappers.SingleArticlePostRequest;
import io.spring.boot.controller.wrappers.SingleArticlePutRequest;
import io.spring.boot.dto.ArticlePostRequestDTO;
import io.spring.boot.dto.ArticlePutRequestDTO;
import io.spring.boot.entity.Article;
import io.spring.boot.entity.Profile;
import io.spring.boot.entity.User;
import io.spring.boot.repository.UserRepository;
import io.spring.boot.security.JwtService;
import io.spring.boot.security.SecurityConfig;
import io.spring.boot.service.ArticleService;
import io.spring.boot.service.UserService;


@WebMvcTest(ArticleRestController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class})
class ArticleControllerUnitTest {
	
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	// Controller calls mocked methods → returns controlled test data instead of DB calls
	@MockBean
	private ArticleService articleService;
	// Used for token generation in controller responses (e.g., login/register).
	@MockBean
    private JwtService jwtService;
	@MockBean
	private UserService userService;
	@MockBean
	private UserRepository userRepository;
	
	private User author;
	private Article article;


	@BeforeEach
	void setup() {
        author = new User(1L, "user@gmail.com", new Profile("username"));
		article = new Article(1L, "title", "description", "body");
		article.setAuthor(author);
	}
	
	@Test
	public void getArticleTest() throws Exception {
		given(articleService.findArticleBySlug(article.getSlug())).willReturn(article);
		
		ResultActions response = mockMvc.perform(get("/api/articles/{slug}", article.getSlug()));

		response.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.article.title", is(article.getTitle())))
					.andExpect(jsonPath("$.article.body", is(article.getBody())));
	}
	
	@Test
	public void createArticleTest() throws Exception {
		Article article2 = new Article(2L, "title2", "description2", "body2");
		article2.setAuthor(author);
		ArticlePostRequestDTO request = new ArticlePostRequestDTO(article2.getTitle(), article2.getDescription(), article2.getBody(), Set.of());
		SingleArticlePostRequest payload = new SingleArticlePostRequest(request);
		// Using "article2" implies the exact article2 instance needs be called vs any Article object with "any(Article.class)"
		given(articleService.saveArticle(any(Article.class))).willReturn(article2);
		
		ResultActions response = mockMvc.perform(post("/api/articles")
											.with(user(author))
											.contentType(MediaType.APPLICATION_JSON)				//specifies the payload format
											.content(objectMapper.writeValueAsString(payload)));	//provides the JSON data for the @RequestBody
		
		response.andDo(print())
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.article.title", is(article2.getTitle())))
					.andExpect(jsonPath("$.article.body", is(article2.getBody())));
	}
	
	@Test
	public void deleteArticleTest() throws Exception {
		given(articleService.findArticleBySlug(article.getSlug())).willReturn(article);
		willDoNothing().given(articleService).deleteByArticleSlug("title");
		
		ResultActions response = mockMvc.perform(delete("/api/articles/{slug}", article.getSlug()).with(user(author)));
		
		response.andDo(print()).andExpect(status().isNoContent());
	}
	
	@Test
	public void updateArticleTest() throws Exception {
		given(articleService.findArticleBySlug("title")).willReturn(article);
		ArticlePutRequestDTO request = new ArticlePutRequestDTO("new title", "new body", null);
		SingleArticlePutRequest payload = new SingleArticlePutRequest(request);
		article.setTitle(request.getTitle());
		article.setBody(request.getBody());
		given(articleService.updateArticle(any(String.class), any(ArticlePutRequestDTO.class))).willReturn(article);
		
		ResultActions response = mockMvc.perform(put("/api/articles/{slug}", "title")
											.with(user(author))
											.contentType(MediaType.APPLICATION_JSON)				//specifies the payload format
											.content(objectMapper.writeValueAsString(payload)));	//provides the JSON data for the @RequestBody
		
		response.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.article.title", is(article.getTitle())))
					.andExpect(jsonPath("$.article.body", is(article.getBody())));
	}
	
	@Test
	public void findArticleByCriteriaTest() throws Exception {
        Page<Article> page = new PageImpl<>(List.of(article), PageRequest.of(0, 20), 1);
		given(articleService.findArticleByCriteria(isNull(), eq("username"), isNull(), any(Pageable.class))).willReturn(page);
		
		ResultActions response = mockMvc.perform(get("/api/articles") 	//explicitly sets query parameters in the HTTP request
											.with(user(author))			//HTTP request = /api/articles?author=username&limit=20&offset=0
								            .param("author", "username")//controller param author=username
								            .param("limit", "20")		//controller param limit=20
								            .param("offset", "0"));		//controller param offset=0
		
		response.andDo(print())
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.articles[0].title", is("title")))
			            .andExpect(jsonPath("$.articles[0].description", is("description")));
		// Body: the endpoints retrieving a list of articles do no longer return the body of an article for performance reasons
	}
	
	@Test
	public void findFeedTest() throws Exception {
		User follower = new User(2L, "follower@gmail.com", new Profile("follower"));
		follower.followUser(author);
		
        Page<Article> page = new PageImpl<>(List.of(article), PageRequest.of(0, 20), 1);
        given(articleService.findFeed(eq(follower.getId()), any(Pageable.class))).willReturn(page); //any(Long.class), any(Pageable.class)
		
		ResultActions response = mockMvc.perform(get("/api/articles/feed")	//explicitly sets query parameters in the HTTP request
						.with(user(follower))								//HTTP request = /api/articles/feed?limit=20&offset=0
						.param("limit", "20")								//controller param limit=20
			            .param("offset", "0"));								//controller param offset=0
		
		response.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.articles[0].title", is("title")))
					.andExpect(jsonPath("$.articles[0].favorited").value(false))
			        .andExpect(jsonPath("$.articles[0].author.following").value(true));
		// Body: the endpoints retrieving a list of articles do no longer return the body of an article for performance reasons
	}
	
	@Test
	public void favoriteArticleTest() throws Exception {
		User follower = new User(2L, "follower@gmail.com", new Profile("follower"));
		article.favoriteArticle(follower);
		given(articleService.favoriteArticle(follower.getId(), article.getSlug())).willReturn(article);
		
        ResultActions response = mockMvc.perform(post("/api/articles/{slug}/favorite", article.getSlug()).with(user(follower)));
		
		response.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.article.favorited", is(true)));
	}
	
	@Test
	public void unfavoriteArticleTest() throws Exception {
		User follower = new User(2L, "follower@gmail.com", new Profile("follower"));
		article.favoriteArticle(follower);
		article.unfavoriteArticle(follower);
		given(articleService.unfavoriteArticle(follower.getId(), article.getSlug())).willReturn(article);
		
        ResultActions response = mockMvc.perform(delete("/api/articles/{slug}/favorite", article.getSlug()).with(user(follower)));
		
		response.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.article.favorited", is(false)));
	}

}
