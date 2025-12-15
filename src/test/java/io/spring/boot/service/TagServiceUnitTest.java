package io.spring.boot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.spring.boot.entity.Tag;
import io.spring.boot.repository.TagRepository;

@ExtendWith(MockitoExtension.class)
public class TagServiceUnitTest {
	
	@Mock
	private TagRepository tagRepository;
	
	@InjectMocks
	private TagService tagService;
	
	private List<Tag> tagList;
	
	@BeforeEach
	void setup() {
		tagList = List.of(new Tag("react"), new Tag("angular"), new Tag("vue")); //"react", "angular", "vue"
	}
	
	@Test
	public void saveTagTest() {
		Tag tag = new Tag("react");
		given(tagRepository.save(tag)).willReturn(tag);
		
		Tag result = tagService.saveTag(tag);
		
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("react");
	}
	
	@Test
	public void findAllTags() {
		given(tagRepository.findAll()).willReturn(tagList);
		
		List<Tag> result = tagService.findAllTags();
		
		assertThat(result).isNotNull();
		assertThat(result).hasSize(3);
		assertThat(result.stream().map(Tag::getName)).containsExactlyInAnyOrder("react", "angular", "vue");
	}
}
