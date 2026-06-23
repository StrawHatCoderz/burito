package com.burito.identity.exceptions;
import com.burito.core.exceptions.APIException;

import com.burito.core.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends APIException {
  public InvalidCredentialsException(String message) {
    super(message, ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
  }
}
