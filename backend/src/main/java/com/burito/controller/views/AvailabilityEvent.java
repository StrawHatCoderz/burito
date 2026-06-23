package com.burito.controller.views;

/** Broadcast payload for restaurant open/closed changes. */
public record AvailabilityEvent(String restaurantId, boolean open, String restaurantName) {}
