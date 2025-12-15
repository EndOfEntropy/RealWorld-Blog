package io.spring.boot.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.spring.boot.dto.TagResponseDTO;
import io.spring.boot.entity.Tag;
import io.spring.boot.service.TagService;

@RestController
@RequestMapping("/api")
public class TagRestController {
	
	private TagService tagService;
	
	@Autowired
	public TagRestController(TagService tagService) {
		this.tagService = tagService;
	}

	@GetMapping("/tags")
	private ResponseEntity<TagResponseDTO> findAllTags(){
        List<String> tagNames = tagService.findAllTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new TagResponseDTO(tagNames));
	}
}
