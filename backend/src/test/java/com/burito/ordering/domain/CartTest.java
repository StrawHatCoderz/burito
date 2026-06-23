package com.burito.ordering.domain;

import com.burito.catalog.domain.Restaurant;
import com.burito.identity.domain.User;
import com.burito.ordering.enums.CartStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CartTest {

    @Test
    void testCartConstructorUser() {
        User user = new User();
        user.setUserId(UUID.randomUUID());

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(UUID.randomUUID());

        Cart cart = new Cart(user, restaurant);

        assertEquals(user, cart.getUser());
        assertNull(cart.getGuestId());
        assertEquals(restaurant, cart.getRestaurant());
        assertEquals(CartStatus.PENDING, cart.getStatus());
        assertEquals(BigDecimal.ZERO, cart.getTotal());
    }

    @Test
    void testCartConstructorGuest() {
        UUID guestId = UUID.randomUUID();

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(UUID.randomUUID());

        Cart cart = new Cart(guestId, restaurant);

        assertNull(cart.getUser());
        assertEquals(guestId, cart.getGuestId());
        assertEquals(restaurant, cart.getRestaurant());
        assertEquals(CartStatus.PENDING, cart.getStatus());
        assertEquals(BigDecimal.ZERO, cart.getTotal());
    }

    @Test
    void testSetters() {
        Cart cart = new Cart();
        UUID id = UUID.randomUUID();
        User user = new User();
        UUID guestId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        
        cart.setCartId(id);
        cart.setUser(user);
        cart.setGuestId(guestId);
        cart.setRestaurant(restaurant);
        cart.setStatus(CartStatus.EXPIRED);
        cart.setTotal(new BigDecimal("100.00"));

        assertEquals(id, cart.getCartId());
        assertEquals(user, cart.getUser());
        assertEquals(guestId, cart.getGuestId());
        assertEquals(restaurant, cart.getRestaurant());
        assertEquals(CartStatus.EXPIRED, cart.getStatus());
        assertEquals(new BigDecimal("100.00"), cart.getTotal());
    }
}
