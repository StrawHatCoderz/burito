package com.burito.exceptions;

import com.burito.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class RestaurantNotFoundException extends APIException {
  public RestaurantNotFoundException(String restaurantId) {
    super("Restaurant not found with id: " + restaurantId,
            ErrorCode.RESTAURANT_NOT_FOUND, HttpStatus.NOT_FOUND);
  }
}