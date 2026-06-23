package com.burito.core.exceptions;

import com.burito.core.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends APIException {
  public ForbiddenException(String message) {
    super(message, ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN);
  }
}
