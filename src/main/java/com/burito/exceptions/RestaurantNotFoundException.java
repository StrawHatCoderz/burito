package com.burito.exceptions;

import com.burito.enums.ErrorCodes;
import lombok.Getter;

@Getter
public class RestaurantNotFoundException extends Exception {

  private final ErrorCodes errorCode;

  public RestaurantNotFoundException(String restaurantId) {
    super("Restaurant not found with id: " + restaurantId);
    this.errorCode = ErrorCodes.RESTAURANT_NOT_FOUND;
  }
}