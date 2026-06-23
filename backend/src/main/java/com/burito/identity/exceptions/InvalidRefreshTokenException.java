package com.burito.identity.exceptions;
import com.burito.core.exceptions.APIException;

import com.burito.core.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends APIException {
  public InvalidRefreshTokenException(String message) {
    super(message, ErrorCode.INVALID_REFRESH_TOKEN, HttpStatus.UNAUTHORIZED);
  }
}
