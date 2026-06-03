package com.burito.exceptions;

import com.burito.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidCuisineTypeException extends APIException {
  public InvalidCuisineTypeException(String value) {
    super("Invalid cuisine type: " + value, ErrorCode.INVALID_CUISINE_TYPE, HttpStatus.BAD_REQUEST);
  }
}
