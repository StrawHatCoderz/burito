package com.burito.controller.views;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Payload for adding an item to the cart")
public record CartItemRequest(
    @Schema(description = "UUID of the menu item to add", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID menuItemId,

    @Schema(description = "Quantity of the menu item to add", example = "1")
    int quantity
) {
}
