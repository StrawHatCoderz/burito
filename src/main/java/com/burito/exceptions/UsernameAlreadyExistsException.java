package com.burito.exceptions;

import com.burito.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class UsernameAlreadyExistsException extends APIException {

  public UsernameAlreadyExistsException() {
    super("username already exists", ErrorCode.USERNAME_ALREADY_EXISTS,
            HttpStatus.BAD_REQUEST);
  }
}
