package com.burito.catalog.exceptions;
import com.burito.core.exceptions.APIException;

import com.burito.core.enums.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class RestaurantNotFoundException extends APIException {
  public RestaurantNotFoundException(UUID restaurantId) {
    super("Restaurant not found with id: " + restaurantId,
            ErrorCode.RESTAURANT_NOT_FOUND, HttpStatus.NOT_FOUND);
  }
}