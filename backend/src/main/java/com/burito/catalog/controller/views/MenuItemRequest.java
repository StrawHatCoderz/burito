package com.burito.catalog.controller.views;

import com.burito.catalog.enums.MenuCategory;
import java.math.BigDecimal;

public record MenuItemRequest(
    String name,
    String description,
    BigDecimal price,
    MenuCategory category,
    boolean isAvailable,
    String imageUrl
) {}
