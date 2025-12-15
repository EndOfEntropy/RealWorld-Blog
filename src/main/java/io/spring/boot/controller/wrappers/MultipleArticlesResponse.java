package io.spring.boot.controller.wrappers;

import java.util.List;

import io.spring.boot.dto.MultipleArticlesResponseDTO;

public record MultipleArticlesResponse(List<MultipleArticlesResponseDTO> articles, Long articlesCount) {

}
