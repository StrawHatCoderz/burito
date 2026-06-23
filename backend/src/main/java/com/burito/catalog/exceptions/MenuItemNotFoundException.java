package com.burito.catalog.exceptions;
import com.burito.core.exceptions.APIException;

import com.burito.core.enums.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class MenuItemNotFoundException extends APIException {
  public MenuItemNotFoundException(UUID menuItemId) {
    super("Menu item not found with id: " + menuItemId,
            ErrorCode.MENU_ITEM_NOT_FOUND, HttpStatus.BAD_REQUEST);
  }
}
