package com.burito.core.exceptions;

import com.burito.core.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class APIException extends RuntimeException {
  private final ErrorCode errorCode;
  private final HttpStatus httpStatus;

  protected APIException(String message,
                      ErrorCode errorCode,
                      HttpStatus httpStatus) {
    super(message);

    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  public static APIException badRequest() {
    return badRequest("Bad Request");
  }

  public static APIException badRequest(String message) {
    return new APIException(message, ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
  }

  public static APIException unauthorized() {
    return unauthorized("Unauthorized access");
  }

  public static APIException unauthorized(String message) {
    return new APIException(message, ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
  }

  public static APIException forbidden() {
    return forbidden("Access forbidden");
  }

  public static APIException forbidden(String message) {
    return new APIException(message, ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN);
  }

  public static APIException notFound() {
    return notFound("Resource not found");
  }

  public static APIException notFound(String message) {
    return new APIException(message, ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND);
  }
}
