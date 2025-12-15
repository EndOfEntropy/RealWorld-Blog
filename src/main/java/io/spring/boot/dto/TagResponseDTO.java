package io.spring.boot.dto;

import java.util.List;

public class TagResponseDTO {
    
	private final List<String> tags;

    public TagResponseDTO(List<String> tagList) {
        this.tags = tagList;
    }

    /**
     * The getter method getTags() tells Jackson to serialize the tags field as "tags" in the JSON output. 
     * Only applies to collection types, not primitive types.
     * (Jackson uses getter names by default for property names unless overridden). So here no need for a wrapper record class.
     */
    public List<String> getTags() {
        return tags;
    }
}
