package com.burito.controller.views;

import com.burito.enums.MenuCategory;
import java.math.BigDecimal;

public record MenuItemRequest(
    String name,
    String description,
    BigDecimal price,
    MenuCategory category,
    boolean isAvailable,
    String imageUrl
) {}
