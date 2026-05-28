package com.burito.exceptions;

import com.burito.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends APIException {
  public InvalidCredentialsException(String message) {
    super(message, ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
  }
}
