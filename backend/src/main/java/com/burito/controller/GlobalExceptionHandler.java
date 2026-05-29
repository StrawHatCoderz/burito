package com.burito.controller;

import com.burito.controller.views.APIResponse;
import com.burito.controller.views.ApiError;
import com.burito.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<APIResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    return ResponseEntity.badRequest()
            .body(APIResponse.error(new ApiError(
                    ErrorCode.INVALID_RESTAURANT_ID,
                    "Invalid ID format: " + e.getValue()
            )));
  }
}
