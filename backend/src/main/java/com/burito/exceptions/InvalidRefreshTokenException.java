package com.burito.exceptions;

import com.burito.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends APIException {
  public InvalidRefreshTokenException(String message) {
    super(message, ErrorCode.INVALID_REFRESH_TOKEN, HttpStatus.UNAUTHORIZED);
  }
}
