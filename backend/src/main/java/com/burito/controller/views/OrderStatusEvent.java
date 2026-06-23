package com.burito.controller.views;

import com.burito.enums.OrderStatus;

/** Broadcast payload for order status transitions. */
public record OrderStatusEvent(String orderId, String customerId, OrderStatus status, String restaurantId) {}
