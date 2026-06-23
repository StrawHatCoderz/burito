package com.burito.catalog.controller.views;

import com.burito.catalog.domain.MenuItem;

/**
 * Broadcast payload for menu item changes.
 * type: ITEM_ADDED | ITEM_UPDATED | ITEM_AVAILABILITY_CHANGED | ITEM_DELETED
 */
public record MenuEvent(String type, String restaurantId, MenuItem item, String menuItemId) {

    /** Factory helpers keep call sites clean. */
    public static MenuEvent added(String restaurantId, MenuItem item) {
        return new MenuEvent("ITEM_ADDED", restaurantId, item, item.getMenuItemId().toString());
    }

    public static MenuEvent updated(String restaurantId, MenuItem item) {
        return new MenuEvent("ITEM_UPDATED", restaurantId, item, item.getMenuItemId().toString());
    }

    public static MenuEvent availabilityChanged(String restaurantId, MenuItem item) {
        return new MenuEvent("ITEM_AVAILABILITY_CHANGED", restaurantId, item, item.getMenuItemId().toString());
    }

    public static MenuEvent deleted(String restaurantId, String menuItemId) {
        return new MenuEvent("ITEM_DELETED", restaurantId, null, menuItemId);
    }
}
