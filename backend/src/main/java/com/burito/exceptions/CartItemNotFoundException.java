package com.burito.exceptions;

import com.burito.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import java.util.UUID;

public class CartItemNotFoundException extends APIException {
    public CartItemNotFoundException(UUID cartItemId) {
        super("Cart item not found: " + cartItemId, ErrorCode.CART_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
