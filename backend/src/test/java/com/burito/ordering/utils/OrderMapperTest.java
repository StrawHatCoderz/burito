package com.burito.ordering.utils;

import com.burito.catalog.domain.Restaurant;
import com.burito.identity.domain.User;
import com.burito.ordering.controller.views.OrderView;
import com.burito.ordering.domain.Order;
import com.burito.ordering.domain.OrderItem;
import com.burito.ordering.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderMapperTest {

    @Test
    void mapToView_success() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setFullName("Test User");

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(UUID.randomUUID());

        Order order = new Order(user, restaurant, 100.0);
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        OrderItem item = new OrderItem(UUID.randomUUID(), "Taco", 50.0, 2);
        item.setId(UUID.randomUUID());
        order.addItem(item);

        OrderView view = OrderMapper.mapToView(order);

        assertNotNull(view);
        assertEquals(order.getId(), view.getId());
        assertEquals(user.getUserId(), view.getCustomerId());
        assertEquals("Test User", view.getCustomerName());
        assertEquals(restaurant.getRestaurantId(), view.getRestaurantId());
        assertEquals(OrderStatus.PENDING, view.getStatus());
        assertEquals(100.0, view.getTotalAmount());
        assertEquals(1, view.getItems().size());
        assertEquals("Taco", view.getItems().get(0).getName());
        assertEquals(50.0, view.getItems().get(0).getPriceAtCheckout());
        assertEquals(2, view.getItems().get(0).getQuantity());
        assertEquals(item.getMenuItemId(), view.getItems().get(0).getMenuItemId());
        assertEquals(item.getId(), view.getItems().get(0).getId());
    }

    @Test
    void mapToView_successWithEmailAsName() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setFullName(null);

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(UUID.randomUUID());

        Order order = new Order(user, restaurant, 100.0);
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);

        OrderView view = OrderMapper.mapToView(order);

        assertNotNull(view);
        assertEquals("test@test.com", view.getCustomerName());
    }
}
