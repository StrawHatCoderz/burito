package com.burito.catalog.exceptions;
import com.burito.core.exceptions.APIException;

import com.burito.core.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidCuisineTypeException extends APIException {
  public InvalidCuisineTypeException(String value) {
    super("Invalid cuisine type: " + value, ErrorCode.INVALID_CUISINE_TYPE, HttpStatus.BAD_REQUEST);
  }
}
