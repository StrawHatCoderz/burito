package com.burito.exceptions;

import com.burito.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class APIException extends Exception {
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
