package com.burito.controller.views;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Details of an individual item in the cart")
public record CartItemView(
    @Schema(description = "UUID of the cart item record")
    UUID cartItemId,

    @Schema(description = "UUID of the associated menu item")
    UUID menuItemId,

    @Schema(description = "Name of the menu item")
    String name,

    @Schema(description = "Quantity added to the cart")
    int quantity,

    @Schema(description = "Price of a single unit of the item")
    BigDecimal unitPrice,

    @Schema(description = "Total cost for this item (unitPrice * quantity)")
    BigDecimal subtotal
) {
}
