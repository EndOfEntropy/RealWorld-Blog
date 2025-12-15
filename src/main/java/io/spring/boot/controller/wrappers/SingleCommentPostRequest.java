package io.spring.boot.controller.wrappers;

import io.spring.boot.dto.CommentPostRequestDTO;

public record SingleCommentPostRequest(CommentPostRequestDTO comment) {

}