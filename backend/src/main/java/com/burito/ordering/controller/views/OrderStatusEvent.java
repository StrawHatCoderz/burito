package com.burito.ordering.controller.views;

import com.burito.ordering.enums.OrderStatus;

/** Broadcast payload for order status transitions. */
public record OrderStatusEvent(String orderId, String customerId, OrderStatus status, String restaurantId) {}
