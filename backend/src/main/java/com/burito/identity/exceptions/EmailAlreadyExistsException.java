package com.burito.identity.exceptions;
import com.burito.core.exceptions.APIException;

import com.burito.core.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends APIException {

  public EmailAlreadyExistsException() {
    super("Email already exists", ErrorCode.EMAIL_ALREADY_EXISTS,
            HttpStatus.CONFLICT);
  }
}
