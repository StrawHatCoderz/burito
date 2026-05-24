package com.burito.exceptions;

import com.burito.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidRestaurantIdException extends APIException {
  public InvalidRestaurantIdException(String restaurantId) {
    super("Invalid Restaurant Id: " + restaurantId,
            ErrorCode.INVALID_RESTAURANT_ID, HttpStatus.BAD_REQUEST);
  }
}
