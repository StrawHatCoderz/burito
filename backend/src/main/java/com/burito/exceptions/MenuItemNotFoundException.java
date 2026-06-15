package com.burito.exceptions;

import com.burito.enums.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class MenuItemNotFoundException extends APIException {
  public MenuItemNotFoundException(UUID menuItemId) {
    super("Menu item not found with id: " + menuItemId,
            ErrorCode.MENU_ITEM_NOT_FOUND, HttpStatus.BAD_REQUEST);
  }
}
