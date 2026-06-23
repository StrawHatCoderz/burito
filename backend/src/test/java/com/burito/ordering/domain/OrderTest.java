package com.burito.ordering.domain;

import com.burito.catalog.domain.Restaurant;
import com.burito.identity.domain.User;
import com.burito.ordering.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderTest {

    @Test
    void testOrderConstructorAndGetters() {
        User user = new User();
        user.setUserId(UUID.randomUUID());

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(UUID.randomUUID());

        Order order = new Order(user, restaurant, 150.0);

        assertEquals(user, order.getCustomer());
        assertEquals(restaurant, order.getRestaurant());
        assertEquals(150.0, order.getTotalAmount());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getItems());
        assertEquals(0, order.getItems().size());
    }

    @Test
    void testSetters() {
        Order order = new Order();
        UUID id = UUID.randomUUID();
        order.setId(id);
        order.setStatus(OrderStatus.ACCEPTED);
        order.setTotalAmount(200.0);

        assertEquals(id, order.getId());
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
        assertEquals(200.0, order.getTotalAmount());
    }

    @Test
    void testAddItem() {
        Order order = new Order();
        OrderItem item = new OrderItem(UUID.randomUUID(), "Taco", 50.0, 2);
        
        order.addItem(item);

        assertEquals(1, order.getItems().size());
        assertEquals(order, item.getOrder());
    }
}
