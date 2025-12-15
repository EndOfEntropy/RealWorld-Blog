package io.spring.boot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.spring.boot.entity.Tag;
import io.spring.boot.repository.TagRepository;

@Service
public class TagService {

	@Autowired
	private TagRepository tagRepository;
	
	
	
	public TagService(TagRepository tagRepository) {
		this.tagRepository = tagRepository;
	}

	@Transactional
	public Tag saveTag(Tag tag) {
		if (tag == null || tag.getName() == null || tag.getName().trim().isEmpty()) {
	        throw new IllegalArgumentException("Name cannot be null or empty");
	    }
		return tagRepository.save(tag);
	}
	
	@Transactional(readOnly = true)
	public List<Tag> findAllTags() {
		return tagRepository.findAll();
	}
	
}
