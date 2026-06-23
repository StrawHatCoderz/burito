package com.burito.core.exceptions;

import com.burito.core.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends APIException {
  public ResourceNotFoundException(String message) {
    super(message, ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND);
  }
}
