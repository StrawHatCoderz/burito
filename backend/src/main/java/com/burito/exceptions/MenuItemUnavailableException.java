package com.burito.exceptions;

import com.burito.enums.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class MenuItemUnavailableException extends APIException {
  public MenuItemUnavailableException(UUID menuItemId) {
    super("Menu item is unavailable with id: " + menuItemId,
            ErrorCode.MENU_ITEM_UNAVAILABLE, HttpStatus.BAD_REQUEST);
  }
}
