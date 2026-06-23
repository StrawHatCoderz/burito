package com.burito.ordering.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderItemTest {

    @Test
    void testOrderItemConstructorAndGetters() {
        UUID menuItemId = UUID.randomUUID();
        OrderItem item = new OrderItem(menuItemId, "Taco", 50.0, 2);

        assertEquals(menuItemId, item.getMenuItemId());
        assertEquals("Taco", item.getName());
        assertEquals(50.0, item.getPriceAtCheckout());
        assertEquals(2, item.getQuantity());
    }

    @Test
    void testSetters() {
        OrderItem item = new OrderItem();
        UUID id = UUID.randomUUID();
        UUID menuItemId = UUID.randomUUID();
        
        item.setId(id);
        item.setMenuItemId(menuItemId);
        item.setName("Burrito");
        item.setPriceAtCheckout(100.0);
        item.setQuantity(3);

        assertEquals(id, item.getId());
        assertEquals(menuItemId, item.getMenuItemId());
        assertEquals("Burrito", item.getName());
        assertEquals(100.0, item.getPriceAtCheckout());
        assertEquals(3, item.getQuantity());
    }
}
