package com.burito.catalog.controller.views;

import com.burito.catalog.domain.MenuItem;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MenuEventTest {

    @Test
    void testMenuEventFactories() {
        MenuItem item = new MenuItem();
        UUID itemId = UUID.randomUUID();
        item.setMenuItemId(itemId);
        
        String restaurantId = UUID.randomUUID().toString();

        MenuEvent added = MenuEvent.added(restaurantId, item);
        assertEquals("ITEM_ADDED", added.type());
        assertEquals(restaurantId, added.restaurantId());
        assertEquals(item, added.item());
        assertEquals(itemId.toString(), added.menuItemId());

        MenuEvent updated = MenuEvent.updated(restaurantId, item);
        assertEquals("ITEM_UPDATED", updated.type());
        assertEquals(restaurantId, updated.restaurantId());
        assertEquals(item, updated.item());
        assertEquals(itemId.toString(), updated.menuItemId());

        MenuEvent availabilityChanged = MenuEvent.availabilityChanged(restaurantId, item);
        assertEquals("ITEM_AVAILABILITY_CHANGED", availabilityChanged.type());
        assertEquals(restaurantId, availabilityChanged.restaurantId());
        assertEquals(item, availabilityChanged.item());
        assertEquals(itemId.toString(), availabilityChanged.menuItemId());

        MenuEvent deleted = MenuEvent.deleted(restaurantId, itemId.toString());
        assertEquals("ITEM_DELETED", deleted.type());
        assertEquals(restaurantId, deleted.restaurantId());
        assertNull(deleted.item());
        assertEquals(itemId.toString(), deleted.menuItemId());
    }
}
