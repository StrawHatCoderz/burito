package com.burito.catalog.controller.views;

/** Broadcast payload for restaurant open/closed changes. */
public record AvailabilityEvent(String restaurantId, boolean open, String restaurantName) {}
