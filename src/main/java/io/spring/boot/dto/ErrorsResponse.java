package io.spring.boot.dto;

import io.spring.boot.controller.wrappers.ErrorsBodyResponse;

public class ErrorsResponse {

 private ErrorsBodyResponse errors;

 // Constructor for ErrorsResponse class
 public ErrorsResponse(ErrorsBodyResponse errorsBodyResponse) {
     this.errors = errorsBodyResponse;
 }

 // Getter for errorsBodyResponse
 public ErrorsBodyResponse getErrors() {
     return errors;
 }

}