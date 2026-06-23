package com.burito.core.exceptions;

import com.burito.core.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class APIException extends RuntimeException {
  private final ErrorCode errorCode;
  private final HttpStatus httpStatus;

  protected APIException(String message,
                      ErrorCode errorCode,
                      HttpStatus httpStatus) {
    super(message);

    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }
}
