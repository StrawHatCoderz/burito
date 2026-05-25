package com.burito.exceptions;

import com.burito.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends APIException {
  public InvalidPasswordException() {
    super("Invalid password",
            ErrorCode.INVALID_PASSWORD,
            HttpStatus.BAD_REQUEST
    );
  }
}
