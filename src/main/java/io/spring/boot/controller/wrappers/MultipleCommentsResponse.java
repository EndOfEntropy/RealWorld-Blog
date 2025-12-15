package io.spring.boot.controller.wrappers;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;

import io.spring.boot.dto.CommentResponseDTO;

public record MultipleCommentsResponse(List<CommentResponseDTO> comments) {

}
