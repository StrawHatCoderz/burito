package com.burito.controller.views;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemView {
    private UUID id;
    private UUID menuItemId;
    private String name;
    private Double priceAtCheckout;
    private Integer quantity;
}
