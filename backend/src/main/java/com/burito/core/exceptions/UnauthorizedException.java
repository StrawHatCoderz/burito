package com.burito.core.exceptions;

import com.burito.core.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends APIException {
  public UnauthorizedException(String message) {
    super(message, ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
  }
}
