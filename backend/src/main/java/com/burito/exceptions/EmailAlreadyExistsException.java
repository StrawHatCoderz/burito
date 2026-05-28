package com.burito.exceptions;

import com.burito.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends APIException {

  public EmailAlreadyExistsException() {
    super("Email already exists", ErrorCode.EMAIL_ALREADY_EXISTS,
            HttpStatus.CONFLICT);
  }
}
