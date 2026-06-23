package com.burito.core.exceptions;

import com.burito.core.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class BadRequestException extends APIException {
  public BadRequestException(String message) {
    super(message, ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
  }
}
