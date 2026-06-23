package com.burito.ordering.utils;

import com.burito.ordering.controller.views.OrderItemView;
import com.burito.ordering.controller.views.OrderView;
import com.burito.ordering.domain.Order;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    private OrderMapper() {
        // Prevent instantiation of utility class
    }

    public static OrderView mapToView(Order order) {
        List<OrderItemView> itemViews = order.getItems().stream()
                .map(item -> new OrderItemView(
                        item.getId(),
                        item.getMenuItemId(),
                        item.getName(),
                        item.getPriceAtCheckout(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        return new OrderView(
                order.getId(),
                order.getCustomer().getUserId(),
                order.getCustomer().getFullName() != null ? order.getCustomer().getFullName() : order.getCustomer().getEmail(),
                order.getRestaurant().getRestaurantId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                itemViews
        );
    }
}
