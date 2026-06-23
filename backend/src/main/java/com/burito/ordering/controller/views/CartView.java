package com.burito.ordering.controller.views;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Details of the user's current cart")
public record CartView(
    @Schema(description = "UUID of the cart, or null if no cart exists")
    UUID cartId,

    @Schema(description = "UUID of the restaurant this cart is associated with, or null if no cart exists")
    UUID restaurantId,

    @Schema(description = "List of items in the cart")
    List<CartItemView> items,

    @Schema(description = "Total price of all items in the cart")
    BigDecimal total
) {
}
