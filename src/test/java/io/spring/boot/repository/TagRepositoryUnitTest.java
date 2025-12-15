package io.spring.boot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import io.spring.boot.entity.Tag;

@DataJpaTest(showSql = false)
@Transactional
public class TagRepositoryUnitTest {

	@Autowired
	private TagRepository tagRepository;
	
	@Test
	public void saveTagTest() {
		Tag tag = new Tag("react");
		Tag result = tagRepository.save(tag);
		
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("react");
	}
	
	@Test
	public void getTagsTest() {
		Tag tag1 = tagRepository.save(new Tag("react"));
		Tag tag2 = tagRepository.save(new Tag("angular"));
		Tag tag3 = tagRepository.save(new Tag("vue"));
		
		List<Tag> tags = tagRepository.findAll();
		
		assertThat(tags).isNotNull();
		assertThat(tags).hasSize(3);
		assertThat(tags).extracting(Tag::getName).containsExactlyInAnyOrder("react", "vue", "angular");
	}
}
