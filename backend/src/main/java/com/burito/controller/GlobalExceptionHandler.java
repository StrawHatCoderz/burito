package com.burito.controller;

import com.burito.controller.views.APIResponse;
import com.burito.controller.views.ApiError;
import com.burito.enums.ErrorCode;
import com.burito.exceptions.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(APIException.class)
  public ResponseEntity<APIResponse<Void>> handleApiException(APIException e) {
    log.warn("API exception [{}]: {}", e.getErrorCode(), e.getMessage());
    return ResponseEntity.status(e.getHttpStatus())
            .body(APIResponse.error(new ApiError(e.getErrorCode(), e.getMessage())));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<APIResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    return ResponseEntity.badRequest()
            .body(APIResponse.error(new ApiError(
                    ErrorCode.INVALID_RESTAURANT_ID,
                    "Invalid ID format: " + e.getValue()
            )));
  }
}
